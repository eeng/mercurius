(ns mercurius.wallets.adapters.repositories.in-memory-wallet-repository
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository find-wallet]]
            [mercurius.util.collections :refer [map-vals sum-by]]))

(defrecord InMemoryWalletRepository [db]
  WalletRepository

  (save-wallet [this {:keys [user-id currency] :as wallet}]
    (alter db assoc-in [user-id currency] (dissoc wallet :last-events))
    (find-wallet this user-id currency))

  (find-wallet [this user-id currency]
    (get-in @db [user-id currency]))

  (get-user-wallets [_ user-id]
    (-> @db (get user-id) vals (or [])))

  (calculate-monetary-base [_]
    (->> @db vals (mapcat vals)
         (group-by :currency)
         (map-vals #(sum-by :balance %)))))

(defn new-in-memory-wallet-repo []
  (InMemoryWalletRepository. (ref {})))
