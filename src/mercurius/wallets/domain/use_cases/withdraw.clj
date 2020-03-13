(ns mercurius.wallets.domain.use-cases.withdraw
  (:require [mercurius.core.domain.use-case :refer [UseCase]]
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
