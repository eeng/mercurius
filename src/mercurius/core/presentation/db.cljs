(ns mercurius.core.presentation.db
  (:require [clojure.spec.alpha :as s]
            [mercurius.trading.presentation.flow :refer [precisions default-place-order-form]]))

(def default-db {:order-book-precision "P0"
                 :place-order-form default-place-order-form})

(s/def ::loading? boolean?)
(s/def ::data any?)
(s/def ::error any?)
(s/def ::loading-state (s/keys :req-un [::loading?]))
(s/def ::success-state (s/keys :req-un [::loading? ::data]))
(s/def ::failure-state (s/keys :req-un [::loading? ::error]))
(s/def ::remote-data (s/or :loading ::loading-state
                           :ok ::success-state
                           :error ::failure-state))

(s/def ::login-form map?)
(s/def ::place-order-form map?)
(s/def ::user-id (s/nilable string?))
(s/def ::auth (s/keys :opt-un [::user-id]))
(s/def ::tickers ::remote-data)
(s/def ::order-book ::remote-data)
(s/def ::trades ::remote-data)
(s/def ::ticker-selected string?)
(s/def ::order-book-precision (set precisions))
(s/def :app/db (s/keys :req-un [::order-book-precision]
                       :opt-un [::login-form
                                ::auth
                                ::tickers
                                ::ticker-selected
                                ::order-book
                                ::trades
                                ::place-order-form]))
