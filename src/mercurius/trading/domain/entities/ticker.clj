(ns mercurius.trading.domain.entities.ticker
  (:require [clojure.spec.alpha :as s]))

(def pairs
  {"BTCUSD" {:first-currency "BTC" :last-currency "USD"}
   "ETHUSD" {:first-currency "ETH" :last-currency "USD"}
   "DAIUSD" {:first-currency "DAI" :last-currency "USD"}
   "XRPUSD" {:first-currency "XRP" :last-currency "USD"}
   "BTCEUR" {:first-currency "BTC" :last-currency "EUR"}})

(def available-tickers (keys pairs))

(s/def ::ticker (set available-tickers))
