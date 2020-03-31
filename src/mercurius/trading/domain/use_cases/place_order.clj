(ns mercurius.trading.domain.use-cases.place-order
  (:require [clojure.spec.alpha :as s]
            [mercurius.accounts.domain.entities.user :as user]
            [mercurius.wallets.domain.entities.wallet :as wallet]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.trading.domain.entities.order :as order :refer [new-order calculate-price calculate-reservation]]))

(s/def ::user-id ::user/id)
(s/def ::side ::order/side)
(s/def ::ticker ::ticker/ticker)
(s/def ::amount ::order/amount)
(s/def ::price ::order/price)
(s/def ::command-common (s/keys :req-un [::user-id ::type ::side ::ticker ::amount]))
(defmulti order-type :type)
(defmethod order-type :limit [_] (s/merge ::command-common (s/keys :req-un [::price])))
(defmethod order-type :market [_] ::command-common)
(s/def ::command (s/multi-spec order-type :type))

(defn new-place-order-use-case
  "Returns a use case that allows to place an order for buy or sell the `ticker` (e.g. BTCUSD).
  The `amount` is considered to be in first currency (BTC in the previous example)
  and `price` (which is the limit price for limit orders) in the second currency (USD)."
  [{:keys [fetch-wallet save-wallet insert-order get-bid-ask publish-event]}]
  (fn [{:keys [user-id] :as command}]
    (s/assert ::command command)
    (let [price (calculate-price command get-bid-ask)
          order (new-order (assoc command :price price))
          reservation (calculate-reservation order)]
      (-> (fetch-wallet user-id (:currency reservation))
          (wallet/reserve (:amount reservation))
          (save-wallet))
      (insert-order order)
      (publish-event [:order-placed order])
      :ok)))
