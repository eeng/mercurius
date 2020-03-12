(ns mercurius.wallets.domain.use-cases.deposit
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [find-or-create-by-user-id-and-currency]]))

(defn deposit [repo {:keys [user-id currency amount]}]
  (let [wallet (find-or-create-by-user-id-and-currency repo user-id currency)]
    (println "Wallet: " wallet)))
