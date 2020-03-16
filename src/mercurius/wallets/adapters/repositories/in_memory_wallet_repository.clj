(ns mercurius.wallets.adapters.repositories.in-memory-wallet-repository
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository get-user-wallets]]
            [mercurius.util.collections :refer [detect]]))

(defrecord InMemoryWalletRepository [db]
  WalletRepository

  (save-wallet [_ {:keys [id] :as wallet}]
    (swap! db assoc id wallet)
    wallet)

  (find-wallet [this user-id currency]
    (->> (get-user-wallets this user-id)
         (detect #(= currency (:currency %)))))

  (get-user-wallets [_ user-id]
    (->> @db vals (filter #(= user-id (:user-id %))))))

(defn new-in-memory-wallet-repo []
  (InMemoryWalletRepository. (atom {})))
