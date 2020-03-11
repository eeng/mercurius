(ns mercurius.wallets.domain.use-cases.deposit
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [find-or-create-for]]))

(defn deposit [repo {:keys [user-id currency amount]}]
  (println "DepositUseCase repo:" repo "amount:" amount)
  (let [wallet (find-or-create-for repo user-id currency)]
    (println "Wallet: " wallet)))
