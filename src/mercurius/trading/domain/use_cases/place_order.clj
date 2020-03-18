(ns mercurius.trading.domain.use-cases.place-order
  (:require [clojure.spec.alpha :as s]
            [mercurius.accounts.domain.entities.user]
            [mercurius.wallets.domain.entities.wallet :as wallet]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.trading.domain.entities.order :refer [reserve-money new-order]]))

(s/def ::user-id :user/id)
(s/def ::type #{:market :limit})
(s/def ::side #{:buy :sell})
(s/def ::ticker ::ticker/ticker)
(s/def ::amount (s/and number? pos?))
(s/def ::price (s/and number? pos?))
(s/def ::command (s/keys :req-un [::user-id ::type ::side ::ticker ::amount ::price]))

(defn new-place-order-use-case
  "Returns a use case that allows to place an order for buy or sell the `ticker` (e.g. BTCUSD).
  The `amount` is considered to be in first currency (BTC in the previous example)
  and `price` (which is the limit price for limit orders) in the second currency (USD)."
  [{:keys [fetch-wallet save-wallet insert-order]}]
  (fn [{:keys [user-id side ticker amount price] :as command}]
    (s/assert ::command command)
    (let [money-to-reserve (reserve-money side amount ticker price)]
      (-> (fetch-wallet user-id (:currency money-to-reserve))
          (wallet/reserve (:amount money-to-reserve))
          (save-wallet))
      (-> command new-order insert-order)
      :ok)))
