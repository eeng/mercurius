(ns mercurius.trading.domain.use-cases.process-trade
  (:require [clojure.spec.alpha :as s]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.trading.domain.entities.order :as order]))

(s/def ::id string?)
(s/def ::ticker ::ticker/ticker)
(s/def ::amount ::order/amount)
(s/def ::price ::order/price)
(s/def ::command (s/keys :req-un [::id ::ticker ::price ::amount]))

(defn new-process-trade-use-case
  "Updates a ticker stats from a trade and stores it."
  [{:keys [update-ticker add-trade publish-event]}]
  (fn [command]
    (s/assert ::command command)
    (add-trade command)
    (let [ticker-stats (update-ticker command)]
      (publish-event [:ticker-updated ticker-stats]))))
