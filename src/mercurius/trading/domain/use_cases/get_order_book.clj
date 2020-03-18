(ns mercurius.trading.domain.use-cases.get-order-book
  (:require [clojure.spec.alpha :as s]
            [mercurius.core.domain.use-case :refer [UseCase]]
            [mercurius.accounts.domain.entities.user]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.trading.domain.repositories.order-book-repository :refer [get-order-book]]))

(defrecord GetOrderBook [repo]
  UseCase
  (execute
    [_ {:keys [ticker] :as command}]
    (s/assert ::command command)
    (get-order-book repo ticker)))

(def get-order-book-use-case map->GetOrderBook)

(s/def ::ticker ::ticker/ticker)
(s/def ::command (s/keys :req-un [::ticker]))
