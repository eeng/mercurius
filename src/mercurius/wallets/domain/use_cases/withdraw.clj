(ns mercurius.wallets.domain.use-cases.withdraw
  (:require [clojure.spec.alpha :as s]
            [mercurius.wallets.domain.entities.wallet :as wallet]
            [mercurius.accounts.domain.entities.user :as user]))

(s/def ::user-id ::user/id)
(s/def ::currency ::wallet/currency)
(s/def ::amount number?)
(s/def ::command (s/keys :req-un [::user-id ::currency ::amount]))

(defn new-withdraw-use-case
  "Returns a use case that allows to withdraw some amount from a wallet."
  [{:keys [fetch-wallet save-wallet]}]
  (fn [{:keys [user-id currency amount] :as command}]
    (s/assert ::command command)
    (-> (fetch-wallet user-id currency)
        (wallet/withdraw amount)
        (save-wallet))))
