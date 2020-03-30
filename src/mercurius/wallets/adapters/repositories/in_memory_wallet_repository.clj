(ns mercurius.wallets.adapters.repositories.in-memory-wallet-repository
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository get-user-wallets]]
            [mercurius.util.collections :refer [detect map-vals sum-by]]
            [mercurius.util.optimistic-concurrency :refer [optimistic-assoc]]))

(defrecord InMemoryWalletRepository [db]
  WalletRepository

  (save-wallet [_ {:keys [id] :as wallet}]
    (swap! db optimistic-assoc id wallet)
    (@db id))

  (find-wallet [this user-id currency]
    (->> (get-user-wallets this user-id)
         (detect #(= currency (:currency %)))))

  (get-user-wallets [_ user-id]
    (->> @db vals (filter #(= user-id (:user-id %)))))

  (calculate-monetary-base [_]
    (->> (vals @db)
         (group-by :currency)
         (map-vals #(sum-by :balance %)))))

(defn new-in-memory-wallet-repo []
  (InMemoryWalletRepository. (atom {})))
