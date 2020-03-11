(ns mercurius.wallets.adapters.repositories.in-memory-wallet-repository
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository]]))

(defrecord InMemoryWalletRepository [db]
  WalletRepository

  (find-or-create-for [_ user-id currency]
    (println "finding")
    {:id "...."})

  (save [_ wallet]
    (println "saving")))

(defn make-in-memory-wallet-repo []
  (InMemoryWalletRepository. (atom {})))
