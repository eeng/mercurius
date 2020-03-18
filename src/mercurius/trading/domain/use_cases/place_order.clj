(ns mercurius.trading.domain.use-cases.place-order
  (:require [clojure.spec.alpha :as s]
            [mercurius.core.domain.use-case :refer [UseCase]]
            [mercurius.accounts.domain.entities.user]
            [mercurius.wallets.domain.entities.wallet :as wallet]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [fetch-wallet save-wallet]]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.trading.domain.entities.order :refer [reserve-money new-order]]
            [mercurius.trading.domain.repositories.order-book-repository :refer [insert-order]]))

;; Submits an order for buy or sell the `ticker` (e.g. BTCUSD).
;; The `amount` is considered to be in first currency (BTC in the previous example)
;; and `price` (which is the limit price for limit orders) in the second currency (USD).
(defrecord PlaceOrder [wallet-repo order-book-repo]
  UseCase
  (execute
    [_ {:keys [user-id type side ticker amount price] :as command}]
    (s/assert ::command command)
    (let [money-to-reserve (reserve-money side amount ticker price)
          order (new-order command)]
      (as-> (fetch-wallet wallet-repo user-id (:currency money-to-reserve)) w
        (wallet/reserve w (:amount money-to-reserve))
        (save-wallet wallet-repo w))
      (insert-order order-book-repo order)
      :ok)))

(def place-order-use-case map->PlaceOrder)

(s/def ::user-id :user/id)
(s/def ::type #{:market :limit})
(s/def ::side #{:buy :sell})
(s/def ::ticker ::ticker/ticker)
(s/def ::amount (s/and number? pos?))
(s/def ::price (s/and number? pos?))
(s/def ::command (s/keys :req-un [::user-id ::type ::side ::ticker ::amount ::price]))
