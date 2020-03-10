(ns mercurius.wallets.adapters.repositories.in-memory-wallet-repository
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository]]
            [integrant.core :as ig]))

(defrecord WalletInMemoryRepository [db]
  WalletRepository

  (find-or-create-for [_ user-id currency]
    (println "finding")
    {:id "...."})

  (save [_ wallet]
    (println "saving")))

(defmethod ig/init-key :wallets.repositories/in-memory [_ _]
  (println "Starting InMemory")
  (WalletInMemoryRepository. (atom {})))
