(ns mercurius.wallets.adapters.repositories.in-memory-wallet-repository
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository find-by-user-id save]]
            [mercurius.lib.collections :refer [detect]]
            [mercurius.lib.uuid :refer [uuid]]))

(defrecord InMemoryWalletRepository [db]
  WalletRepository

  (save [_ {:keys [id] :as wallet}]
    (swap! db assoc id wallet)
    wallet)

  (find-by-user-id [_ user-id]
    (->> @db vals (filter #(= user-id (:user-id %)))))

  (find-or-create-by-user-id-and-currency [this user-id currency]
    (if-let [wallet (->> (find-by-user-id this user-id)
                         (detect #(= currency (:currency %))))]
      wallet
      (save this {:id (uuid) :user-id user-id :currency currency}))))

(defn make-in-memory-wallet-repo []
  (InMemoryWalletRepository. (atom {})))
