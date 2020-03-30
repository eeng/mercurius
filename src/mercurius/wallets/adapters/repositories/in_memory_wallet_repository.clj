(ns mercurius.wallets.adapters.repositories.in-memory-wallet-repository
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository find-wallet]]
            [mercurius.util.collections :refer [map-vals sum-by]]
            [mercurius.util.optimistic-concurrency :refer [optimistic-assoc]]))

(defrecord InMemoryWalletRepository [db]
  WalletRepository

  (save-wallet [this {:keys [user-id currency] :as wallet}]
    (alter db optimistic-assoc [user-id currency] wallet)
    (find-wallet this user-id currency))

  (find-wallet [this user-id currency]
    (@db [user-id currency]))

  (get-user-wallets [_ user-id]
    (->> @db vals (filter #(= user-id (:user-id %)))))

  (calculate-monetary-base [_]
    (->> (vals @db)
         (group-by :currency)
         (map-vals #(sum-by :balance %)))))

(defn new-in-memory-wallet-repo []
  (InMemoryWalletRepository. (ref {})))
