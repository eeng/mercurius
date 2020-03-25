(ns mercurius.trading.domain.entities.ticker
  (:require [clojure.spec.alpha :as s]
            [mercurius.util.number :refer [round-to-significant-figures]]))

(def pairs
  {"BTCUSD" {:first-currency "BTC" :last-currency "USD"}
   "ETHUSD" {:first-currency "ETH" :last-currency "USD"}
   "DAIUSD" {:first-currency "DAI" :last-currency "USD"}
   "XRPUSD" {:first-currency "XRP" :last-currency "USD"}
   "BTCEUR" {:first-currency "BTC" :last-currency "EUR"}})

(def available-tickers (keys pairs))

(s/def ::ticker (set available-tickers))

(defn first-currency [ticker]
  (get-in pairs [ticker :first-currency]))

(defn last-currency [ticker]
  (get-in pairs [ticker :last-currency]))

(def currencies (juxt first-currency last-currency))

(def significant-figures 5)

(defn round-number [num]
  (round-to-significant-figures num significant-figures))

(comment
  (currencies "BTCUSD"))
