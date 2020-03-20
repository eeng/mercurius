(ns mercurius.wallets.domain.use-cases.get-wallet
  (:require [clojure.spec.alpha :as s]
            [mercurius.accounts.domain.entities.user :as user]))

(s/def ::user-id ::user/id)
(s/def ::currency :wallet/currency)
(s/def ::command (s/keys :req-un [::user-id ::currency]))

(defn new-get-wallet-use-case
  "Returns a use case that allows to get a user's wallet."
  [{:keys [fetch-wallet]}]
  (fn [{:keys [user-id currency] :as command}]
    (s/assert ::command command)
    (fetch-wallet user-id currency)))
