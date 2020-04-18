(ns mercurius.core.presentation.db
  (:require [clojure.spec.alpha :as s]
            [mercurius.accounts.presentation.login.flow :refer [default-login-form]]
            [mercurius.trading.presentation.order-book.flow :refer [precisions]]
            [mercurius.trading.presentation.place-order.flow :refer [default-place-order-form]]
            [mercurius.trading.presentation.deposit.flow :refer [default-deposit-form]]))

(def default-db {:order-book-precision "P0"
                 :place-order-form default-place-order-form
                 :login-form default-login-form
                 :deposit-form default-deposit-form})

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
(s/def ::user-id (s/nilable string?))
(s/def ::auth (s/keys :req-un [::user-id]))

(s/def ::tickers ::remote-data)
(s/def ::order-book ::remote-data)
(s/def ::trades ::remote-data)
(s/def ::wallets ::remote-data)
(s/def ::ticker-selected string?)
(s/def ::order-book-precision (set precisions))
(s/def ::place-order-form map?)

(s/def :app/db (s/keys :req-un [::login-form
                                ::order-book-precision
                                ::place-order-form]
                       :opt-un [::auth
                                ::tickers
                                ::ticker-selected
                                ::order-book
                                ::trades
                                ::wallets]))
