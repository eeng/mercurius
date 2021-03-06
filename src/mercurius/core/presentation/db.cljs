(ns mercurius.core.presentation.db
  (:require [clojure.spec.alpha :as s]
            [mercurius.accounts.presentation.login.flux :refer [default-login-form]]
            [mercurius.trading.presentation.order-book.flux :refer [precisions]]
            [mercurius.trading.presentation.place-order.flux :refer [default-place-order-form]]
            [mercurius.trading.presentation.deposit.flux :refer [default-deposit-form]]
            [mercurius.trading.presentation.simulate.flux :refer [default-simulate-form]]))

(def default-db {:order-book-precision "P0"
                 :place-order-form default-place-order-form
                 :login-form default-login-form
                 :deposit-form default-deposit-form
                 :simulate-form default-simulate-form})

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
(s/def ::simulate-form map?)

(s/def :app/db (s/keys :req-un [::login-form
                                ::order-book-precision
                                ::place-order-form
                                ::simulate-form]
                       :opt-un [::auth
                                ::tickers
                                ::ticker-selected
                                ::order-book
                                ::trades
                                ::wallets]))
