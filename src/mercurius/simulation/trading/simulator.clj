(ns mercurius.simulation.trading.simulator
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [roul.random :as rr]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.wallets.domain.entities.wallet :refer [available-balance]]))

(defmacro in-section [& body]
  `(do (println (str/join (repeat 80 "=")))
       ~@body
       (println (str/join (repeat 80 "=")))))

(defn- report-config [config]
  (in-section
   (println "Starting simulation with config:" config)))

(defn- report-summary [stage dispatch]
  (in-section
   (println stage "monetary base:" (dispatch :calculate-monetary-base {}))))

(defn user-id-gen []
  (let [last-user-id (atom 0)]
    (fn []
      (swap! last-user-id inc))))

(defn- make-trader [gen-user-id]
  (gen-user-id))

(defn- fund-accounts [traders currencies dispatch]
  (doseq [trader traders
          currency currencies]
    (dispatch :deposit {:user-id trader
                        :amount 10000
                        :currency currency})))

(defn- pick-side []
  (rand-nth [:buy :sell]))

(defn- pick-ticker [tickers]
  (rand-nth (keys tickers)))

(defn- select-src-currency [ticker side]
  (let [[sell-cur buy-cur] (ticker/currencies ticker)]
    (case side
      :buy buy-cur
      :sell sell-cur)))

(defn- pick-price [last-price side [worse-price-pct better-price-pct]]
  (let [buy? (= side :buy)
        min-price (* last-price ((if buy? - +) 1 worse-price-pct))
        max-price (* last-price ((if buy? + -) 1 better-price-pct))]
    (rr/rand min-price max-price)))

(defn- find-current-balance [trader currency dispatch]
  (-> (dispatch :get-wallet {:user-id trader :currency currency})
      (available-balance)))

(defn- calculate-amount [position-size price side]
  (case side
    :buy (/ position-size price)
    :sell position-size))

(defn- place-order [trader dispatch {:keys [tickers pos-size-pct spread-around-better-price]
                                     :or {spread-around-better-price [0.2 0.01]}}]
  (let [ticker (pick-ticker tickers)
        side (pick-side)
        src-currency (select-src-currency ticker side)
        current-balance (find-current-balance trader src-currency dispatch)
        position-size (* (pos-size-pct) current-balance)
        price (pick-price (get-in tickers [ticker :initial-price]) side spread-around-better-price)
        amount (calculate-amount position-size price side)]
    (dispatch :place-order {:user-id trader
                            :side side
                            :amount amount
                            :ticker ticker
                            :price price
                            :type :limit})))

(defn- start-trading [trader dispatch {:keys [n-orders-per-trader]
                                       :or {n-orders-per-trader 1}
                                       :as config}]
  #_(Thread/sleep (* 3000 (rand)))
  (doseq [_ (range n-orders-per-trader)]
    (place-order trader dispatch config)))

(defn run-simulation [{:keys [dispatch]} &
                      {:keys [n-traders tickers]
                       :as config}]
  (s/assert ::config config)
  (report-config config)

  (let [currencies (mapcat ticker/currencies (keys tickers))
        traders (repeatedly n-traders (partial make-trader (user-id-gen)))]
    (fund-accounts traders currencies dispatch)
    (report-summary "Initial" dispatch)
    (doall (pmap #(start-trading % dispatch config) traders)))

  (report-summary "Final" dispatch))

(s/def ::percent (s/and number? #(<= 0 % 1)))
(s/def ::initial-price (s/and number? pos?))
(s/def ::tickers (s/map-of ::ticker/ticker (s/keys :req-un [::initial-price])))
(s/def ::n-traders pos-int?)
(s/def ::n-orders-per-trader pos-int?)
(s/def ::pos-size-pct (s/fspec :args (s/cat) :ret ::percent))
(s/def ::spread-around-better-price (s/cat :worse ::percent :better ::percent))
(s/def ::config (s/keys :req-un [::tickers
                                 ::n-traders
                                 ::n-orders-per-trader
                                 ::pos-size-pct
                                 ::spread-around-better-price]))
