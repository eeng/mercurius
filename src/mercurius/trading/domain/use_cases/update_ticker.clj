(ns mercurius.trading.domain.use-cases.update-ticker
  (:require [clojure.spec.alpha :as s]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.trading.domain.entities.order :as order]))

(s/def ::ticker ::ticker/ticker)
(s/def ::amount ::order/amount)
(s/def ::price ::order/price)
(s/def ::command (s/keys :req-un [::ticker ::price ::amount]))

(defn new-update-ticker-use-case
  "Updates a ticker stats from a trade."
  [{:keys [update-ticker publish-event]}]
  (fn [command]
    (s/assert ::command command)
    (let [ticker-stats (update-ticker command)]
      (publish-event [:ticker-changed ticker-stats]))))
