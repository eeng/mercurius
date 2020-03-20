(ns mercurius.trading.domain.entities.trade
  (:require [tick.alpha.api :as t]))

(defrecord Trade [price amount created-at])

(def new-trade map->Trade)

(defn generate-trade
  "If the bid price is greater or equal to the ask price, a trade is returned.
  Otherwise returns nil."
  [{bid-price :price bid-amount :amount bid-placed-at :placed-at ticker :ticker :as bid}
   {ask-price :price ask-amount :amount ask-placed-at :placed-at :as ask}]
  (when (>= bid-price ask-price)
    (let [trade-price (if (t/< bid-placed-at ask-placed-at) ask-price bid-price)
          trade-amount (min bid-amount ask-amount)]
      (new-trade {:price trade-price
                  :amount trade-amount
                  :ticker ticker
                  :created-at (t/now)
                  :bid bid
                  :ask ask}))))
