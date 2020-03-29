(ns mercurius.trading.domain.use-cases.execute-trades
  (:require [clojure.spec.alpha :as s]
            [mercurius.trading.domain.entities.order :as order :refer [partially-filled?]]
            [mercurius.trading.domain.entities.trade :refer [match-orders build-transfers]]
            [mercurius.trading.domain.entities.ticker :as ticker]))

(s/def ::ticker ::ticker/ticker)
(s/def ::command (s/keys :req-un [::ticker]))

(defn- update-order-book [{:keys [update-order remove-order]} order]
  (if (partially-filled? order)
    (update-order order)
    (remove-order order)))

(defn new-execute-trades-use-case
  "Returns a use case that match bid an ask orders to discover trades, and executes them.
  For each trade, a transfer is made between buyer and seller for each pais's currency.
  Finally the order book is updated."
  [{:keys [get-bids-asks transfer publish-event] :as deps}]
  (fn [{:keys [ticker] :as command}]
    (s/assert ::command command)
    (let [{:keys [bids asks]} (get-bids-asks ticker)
          trades (match-orders bids asks)]
      (doseq [{:keys [bid ask] :as trade} trades]
        (doseq [transfer-data (build-transfers trade)]
          (transfer transfer-data))
        (doseq [order [bid ask]]
          (update-order-book deps order))
        (publish-event [:trade-made trade]))
      trades)))
