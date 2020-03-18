(ns mercurius.trading.domain.use-cases.get-order-book
  (:require [clojure.spec.alpha :as s]
            [mercurius.accounts.domain.entities.user]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.trading.domain.repositories.order-book-repository :refer [get-order-book]]))

(s/def ::ticker ::ticker/ticker)
(s/def ::command (s/keys :req-un [::ticker]))

(defn new-get-order-book-use-case
  "Returns a use case that provides access to the order book of a ticker."
  [{:keys [repo]}]
  (fn [{:keys [ticker] :as command}]
    (s/assert ::command command)
    (get-order-book repo ticker)))
