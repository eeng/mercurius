(ns mercurius.trading.domain.use-cases.match-orders
  (:require [clojure.spec.alpha :as s]
            [mercurius.trading.domain.entities.order :as order]))

(s/def ::bid ::order/order)
(s/def ::ask ::order/order)
(s/def ::command (s/keys :req-un [::bid ::ask]))

(defn new-match-orders-use-case
  "Returns a use case that match bid an ask orders to see if a trade is made."
  [{:keys [fetch-wallet save-wallet]}]
  (fn [{:keys [bid ask] :as command}]
    (s/assert ::command command)))
