(ns mercurius.trading.domain.entities.trade
  (:require [tick.alpha.api :as t]))

(defrecord Trade [id price amount created-at])

(def new-trade map->Trade)

(defn generate-trade
  "If the bid price is greater or equal to the bid price, a trade is returns.
  Otherwise returns nil."
  [{bid-price :price bid-placed-at :placed-at bid-amount :amount ticker :ticker}
   {ask-price :price ask-placed-at :placed-at ask-amount :amount}]
  (when (>= bid-price ask-price)
    (let [trade-price (if (t/< bid-placed-at ask-placed-at) ask-price bid-price)
          trade-amount (min bid-amount ask-amount)]
      (new-trade {:price trade-price
                  :amount trade-amount
                  :ticker ticker
                  :created-at (t/now)}))))
