(ns mercurius.simulation.simulator
  (:require [clojure.string :as str]
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

(defn- select-src-currency [ticker side]
  (let [[sell-cur buy-cur] (ticker/currencies ticker)]
    (case side
      :buy buy-cur
      :sell sell-cur)))

(defn- pick-price [last-price side]
  (let [better-price-gap 0.01
        worse-price-gap 0.10
        buy? (= side :buy)
        min-price (* last-price ((if buy? - +) 1 worse-price-gap))
        max-price (* last-price ((if buy? + -) 1 better-price-gap))]
    (rr/rand min-price max-price)))

(defn- calculate-amount [position-size price side]
  (case side
    :buy (/ position-size price)
    :sell position-size))

(defn- place-order [trader dispatch {:keys [tickers pos-size-pct]}]
  (let [ticker (rand-nth (keys tickers))
        side (pick-side)
        src-currency (select-src-currency ticker side)
        src-wallet (dispatch :get-wallet {:user-id trader :currency src-currency})
        position-size (* (pos-size-pct) (available-balance src-wallet))
        price (pick-price (get-in tickers [ticker :initial-price]) side)
        amount (calculate-amount position-size price side)]
    (dispatch :place-order {:user-id trader
                            :side side
                            :amount amount
                            :ticker ticker
                            :price price
                            :type :limit})))

(defn- start-trading [trader dispatch config]
  #_(Thread/sleep (* 3000 (rand)))
  (place-order trader dispatch config))

(defn run-simulation [{:keys [dispatch]} &
                      {:keys [n-traders tickers]
                       :as config}]
  (report-config config)

  (let [currencies (mapcat ticker/currencies (keys tickers))
        traders (repeatedly n-traders (partial make-trader (user-id-gen)))]
    (fund-accounts traders currencies dispatch)
    (report-summary "Initial" dispatch)
    (doall (pmap #(start-trading % dispatch config) traders)))

  (report-summary "Final" dispatch))
