(ns mercurius.trading.domain.use-cases.place-order
  (:require [clojure.spec.alpha :as s]
            [mercurius.accounts.domain.entities.user :as user]
            [mercurius.wallets.domain.entities.wallet :as wallet]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.trading.domain.entities.order :as order :refer [calculate-reservation new-order]]))

(s/def ::user-id ::user/id)
(s/def ::type ::order/type)
(s/def ::side ::order/side)
(s/def ::ticker ::ticker/ticker)
(s/def ::amount ::order/amount)
(s/def ::price ::order/price)
(s/def ::command (s/keys :req-un [::user-id ::type ::side ::ticker ::amount ::price]))

(defn new-place-order-use-case
  "Returns a use case that allows to place an order for buy or sell the `ticker` (e.g. BTCUSD).
  The `amount` is considered to be in first currency (BTC in the previous example)
  and `price` (which is the limit price for limit orders) in the second currency (USD)."
  [{:keys [fetch-wallet save-wallet insert-order publish-event]}]
  (fn [{:keys [user-id] :as command}]
    (s/assert ::command command)
    (let [order (new-order command)
          reservation (calculate-reservation order)]
      (-> (fetch-wallet user-id (:currency reservation))
          (wallet/reserve (:amount reservation))
          (save-wallet))
      (insert-order order)
      (publish-event [:order-placed order])
      :ok)))
