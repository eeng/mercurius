(ns mercurius.wallets.adapters.repositories.in-memory-wallet-repository
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository get-user-wallets save-wallet]]
            [mercurius.wallets.domain.entities.wallet :refer [new-wallet]]
            [mercurius.lib.collections :refer [detect]]))

(defrecord InMemoryWalletRepository [db]
  WalletRepository

  (save-wallet [_ {:keys [id] :as wallet}]
    (swap! db assoc id wallet)
    wallet)

  (load-wallet [this user-id currency]
    (if-let [wallet (->> (get-user-wallets this user-id)
                         (detect #(= currency (:currency %))))]
      wallet
      (save-wallet this (new-wallet {:user-id user-id :currency currency}))))

  (get-user-wallets [_ user-id]
    (->> @db vals (filter #(= user-id (:user-id %))))))

(defn new-in-memory-wallet-repo []
  (InMemoryWalletRepository. (atom {})))
