(ns mercurius.wallets.domain.use-cases.deposit
  (:require [mercurius.core.domain.use-case :refer [UseCase]]
            [mercurius.wallets.domain.entities.wallet :as wallet]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [load-wallet save-wallet]]))

(defrecord Deposit [repo]
  UseCase
  (execute [_ {:keys [user-id currency amount] :as command}]
    (as-> (load-wallet repo user-id currency) w
      (wallet/deposit w amount)
      (save-wallet repo w))))

(defn deposit-use-case [repo]
  (Deposit. repo))
