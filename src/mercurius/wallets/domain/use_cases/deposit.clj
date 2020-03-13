(ns mercurius.wallets.domain.use-cases.deposit
  (:require [clojure.spec.alpha :as s]
            [mercurius.core.domain.use-case :refer [UseCase request-type]]
            [mercurius.wallets.domain.entities.wallet :as wallet]
            [mercurius.accounts.domain.entities.user]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [load-wallet save-wallet]]))

(defrecord Deposit [repo]
  UseCase
  (execute [_ {:keys [user-id currency amount] :as command}]
    (as-> (load-wallet repo user-id currency) w
      (wallet/deposit w amount)
      (save-wallet repo w))))

(defn deposit-use-case [repo]
  (Deposit. repo))

(s/def ::user-id :user/id)
(s/def ::currency :wallet/currency)
(s/def ::amount number?)
(defmethod request-type :wallets/deposit [_]
  (s/keys :req-un [::user-id ::currency ::amount]))
