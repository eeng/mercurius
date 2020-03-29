(ns mercurius.wallets.adapters.repositories.in-memory-wallet-repository
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository get-user-wallets]]
            [mercurius.util.collections :refer [detect map-vals sum-by assoc-or]]))

(defn inc-version-if-same [new-val old-val]
  (let [old-val (assoc-or old-val :version 0)
        new-val (assoc-or new-val :version 0)]
    (if (= (:version new-val) (:version old-val))
      (update new-val :version inc)
      (throw (IllegalStateException.
              (format "Stale object. Expected version %d but got %d. Object: %s"
                      (:version old-val)
                      (:version new-val)
                      (pr-str new-val)))))))

(defn optimistic-assoc [m k new-val]
  (update m k (partial inc-version-if-same new-val)))

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
