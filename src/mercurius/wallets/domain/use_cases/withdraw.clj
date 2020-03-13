(ns mercurius.wallets.domain.use-cases.withdraw
  (:require [clojure.spec.alpha :as s]
            [mercurius.core.domain.use-case :refer [UseCase request-type]]
            [mercurius.wallets.domain.entities.wallet :as wallet]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [load-wallet save-wallet]]))

(defrecord Withdraw [repo]
  UseCase
  (execute [_ {:keys [user-id currency amount] :as command}]
    (as-> (load-wallet repo user-id currency) w
      (wallet/withdraw w amount)
      (save-wallet repo w))))

(defn withdraw-use-case [repo]
  (Withdraw. repo))

(s/def ::user-id int?)
(s/def ::currency :wallet/currency)
(s/def ::amount number?)
(defmethod request-type :wallets/withdraw [_]
  (s/keys :req-un [::user-id ::currency ::amount]))
