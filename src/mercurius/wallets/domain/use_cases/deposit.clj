(ns mercurius.wallets.domain.use-cases.deposit
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [load-wallet]]))

(defn deposit [repo {:keys [user-id currency amount]}]
  (let [wallet (load-wallet repo user-id currency)]
    (println "Wallet: " wallet)))
