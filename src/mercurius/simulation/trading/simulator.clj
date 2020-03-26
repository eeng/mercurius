(ns mercurius.simulation.trading.simulator
  (:require [clojure.spec.alpha :as s]
            [roul.random :as rr]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.wallets.domain.entities.wallet :refer [available-balance]]))

(defn user-id-gen []
  (let [last-user-id (atom 0)]
    (fn []
      (swap! last-user-id inc))))

(defn- make-trader [gen-user-id]
  (gen-user-id))

(defn- fund-accounts [trader tickers-opts dispatch]
  (doseq [[ticker {:keys [initial-funds initial-price]}] tickers-opts
          :let [[first-cur second-cur] (ticker/currencies ticker)]]
    (dispatch :deposit {:user-id trader
                        :amount (ticker/round-number (/ (float initial-funds) initial-price))
                        :currency first-cur})
    (dispatch :deposit {:user-id trader
                        :amount initial-funds
                        :currency second-cur})))

(defn- pick-side []
  (rand-nth [:buy :sell]))

(defn- pick-ticker [tickers-opts]
  (rand-nth (keys tickers-opts)))

(defn- pick-price [last-price side [worse-price-pct better-price-pct]]
  (let [buy? (= side :buy)
        min-price (* last-price ((if buy? - +) 1 worse-price-pct))
        max-price (* last-price ((if buy? + -) 1 better-price-pct))]
    (-> (rr/rand min-price max-price)
        (ticker/round-number))))

(defn- select-src-currency [ticker side]
  (let [[sell-cur buy-cur] (ticker/currencies ticker)]
    (case side
      :buy buy-cur
      :sell sell-cur)))

(defn- find-current-balance [trader ticker side dispatch]
  (let [currency (select-src-currency ticker side)]
    (-> (dispatch :get-wallet {:user-id trader :currency currency})
        (available-balance))))

(defn- calculate-amount [position-size price side]
  (-> (case side
        :buy (/ position-size price)
        :sell position-size)
      (ticker/round-number)))

(defn- calculate-position-size [max-pos-size-pct current-balance]
  (* (rand max-pos-size-pct) current-balance))

(defn- place-order [trader dispatch {:keys [tickers max-pos-size-pct spread-around-better-price]
                                     :or {max-pos-size-pct 0.3
                                          spread-around-better-price [0.2 0.01]}}]
  (let [ticker (pick-ticker tickers)
        side (pick-side)
        current-balance (find-current-balance trader ticker side dispatch)
        position-size (calculate-position-size max-pos-size-pct current-balance)
        price (pick-price (get-in tickers [ticker :initial-price]) side spread-around-better-price)
        amount (calculate-amount position-size price side)]
    (dispatch :place-order {:user-id trader
                            :side side
                            :amount amount
                            :ticker ticker
                            :price price
                            :type :limit})))

(defn- start-trading [trader dispatch {:keys [tickers n-orders-per-trader max-ms-between-orders]
                                       :or {n-orders-per-trader 1
                                            max-ms-between-orders 0}
                                       :as config}]
  (fund-accounts trader tickers dispatch)
  (doseq [_ (range n-orders-per-trader)]
    (Thread/sleep (rand max-ms-between-orders))
    (place-order trader dispatch config)))

(defn run-simulation [{:keys [dispatch]} & {:keys [n-traders] :as config}]
  (s/assert ::config config)
  (let [traders (repeatedly n-traders (partial make-trader (user-id-gen)))]
    (doall (pmap #(start-trading % dispatch config) traders)))
  :done)

(s/def ::percent (s/and number? #(<= 0 % 1)))
(s/def ::initial-price (s/and number? pos?))
(s/def ::initial-funds (s/and number? pos?))
(s/def ::tickers (s/map-of ::ticker/ticker (s/keys :req-un [::initial-price ::initial-funds])))
(s/def ::n-traders pos-int?)
(s/def ::n-orders-per-trader pos-int?)
(s/def ::max-pos-size-pct ::percent)
(s/def ::max-ms-between-orders int?)
(s/def ::spread-around-better-price (s/cat :worse ::percent :better ::percent))
(s/def ::config (s/keys :req-un [::tickers
                                 ::n-traders
                                 ::n-orders-per-trader]
                        :opt-un [::max-ms-between-orders
                                 ::max-pos-size-pct
                                 ::spread-around-better-price]))
