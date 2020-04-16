(ns mercurius.trading.domain.use-cases.process-trade
  (:require [clojure.spec.alpha :as s]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.trading.domain.entities.order :as order]
            [mercurius.trading.domain.entities.trade :refer [calculate-price-direction]]))

(s/def ::id string?)
(s/def ::ticker ::ticker/ticker)
(s/def ::amount ::order/amount)
(s/def ::price ::order/price)
(s/def ::created-at any?)
(s/def ::command (s/keys :req-un [::id ::created-at ::ticker ::price ::amount]))

(defn- adapt-for-storage [trade prev-price]
  (-> (dissoc trade :bid :ask)
      (assoc :direction (calculate-price-direction trade prev-price))))

(defn new-process-trade-use-case
  "Updates a ticker stats from a trade and stores it."
  [{:keys [update-ticker get-ticker add-trade publish-events]}]
  (fn [{:keys [ticker] :as command}]
    (s/assert ::command command)
    (let [{:keys [last-price]} (get-ticker ticker)
          trade (adapt-for-storage command last-price)
          ticker-stats (update-ticker trade)]
      (add-trade trade)
      (publish-events [[:trade-processed trade]
                       [:ticker-updated ticker-stats]]))))
