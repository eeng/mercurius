(ns mercurius.wallets.domain.use-cases.withdraw
  (:require [clojure.spec.alpha :as s]
            [mercurius.core.domain.use-case :refer [UseCase]]
            [mercurius.wallets.domain.entities.wallet :as wallet]
            [mercurius.accounts.domain.entities.user]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [fetch-wallet save-wallet]]))

(defrecord Withdraw [repo]
  UseCase
  (execute [_ {:keys [user-id currency amount] :as command}]
    (s/assert ::command command)
    (as-> (fetch-wallet repo user-id currency) w
      (wallet/withdraw w amount)
      (save-wallet repo w))))

(def withdraw-use-case map->Withdraw)

(s/def ::user-id :user/id)
(s/def ::currency :wallet/currency)
(s/def ::amount number?)
(s/def ::command (s/keys :req-un [::user-id ::currency ::amount]))
