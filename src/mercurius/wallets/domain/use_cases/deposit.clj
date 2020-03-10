(ns mercurius.wallets.domain.use-cases.deposit
  (:require [mercurius.core.domain.use-case :refer [UseCase]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [find-or-create-for]]
            [integrant.core :as ig]))

(defrecord DepositUseCase [repo]
  UseCase

  (execute [_ {:keys [user-id currency amount]}]
    (println "DepositUseCase repo:" repo "amount:" amount)
    (let [wallet (find-or-create-for repo user-id currency)]
      (println "Wallet: " wallet))))

(defmethod ig/init-key :wallets.use-cases/deposit [_ deps]
  (println "Starting DepositUseCase")
  (map->DepositUseCase deps))
