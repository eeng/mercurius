(ns mercurius.wallets.domain.use-cases.get-wallet
  (:require [clojure.spec.alpha :as s]
            [mercurius.wallets.domain.entities.wallet :as wallet]
            [mercurius.accounts.domain.entities.user :as user]))

(s/def ::user-id ::user/id)
(s/def ::currency ::wallet/currency)
(s/def ::command (s/keys :req-un [::user-id ::currency]))

(defn new-get-wallet-use-case
  "Returns a use case that allows to get a user's wallet."
  [{:keys [load-wallet]}]
  (fn [{:keys [user-id currency] :as command}]
    (s/assert ::command command)
    (load-wallet user-id currency)))
