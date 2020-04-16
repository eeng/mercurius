(ns mercurius.trading.domain.use-cases.process-trade
  (:require [clojure.spec.alpha :as s]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.trading.domain.entities.order :as order]))

(s/def ::id string?)
(s/def ::ticker ::ticker/ticker)
(s/def ::amount ::order/amount)
(s/def ::price ::order/price)
(s/def ::created-at any?)
(s/def ::command (s/keys :req-un [::id ::created-at ::ticker ::price ::amount]))

(defn- adapt-for-storage [trade]
  (dissoc trade :bid :ask))

(defn new-process-trade-use-case
  "Updates a ticker stats from a trade and stores it."
  [{:keys [update-ticker add-trade publish-events]}]
  (fn [command]
    (s/assert ::command command)
    (let [trade (adapt-for-storage command)
          ticker-stats (update-ticker trade)]
      (add-trade trade)
      (publish-events [[:trade-processed trade]
                       [:ticker-updated ticker-stats]]))))
