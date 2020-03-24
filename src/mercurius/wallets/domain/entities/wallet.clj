(ns mercurius.wallets.domain.entities.wallet
  (:require [slingshot.slingshot :refer [throw+]]
            [mercurius.util.uuid :refer [uuid]]
            [clojure.spec.alpha :as s]))

(def available-currencies #{"USD" "EUR" "BTC" "ETH"})

(s/def ::currency available-currencies)

(defrecord Wallet [id user-id currency balance reserved])

(defn new-wallet [{:keys [user-id currency balance reserved] :or {balance 0 reserved 0}}]
  (s/assert ::currency currency)
  (map->Wallet {:id (uuid)
                :user-id user-id
                :currency currency
                :balance (bigdec balance)
                :reserved (bigdec reserved)}))

(defn available-balance [{:keys [balance reserved]}]
  (- balance reserved))

(defn deposit [wallet amount]
  (when (<= amount 0)
    (throw+ {:type :wallet/invalid-amount :amount amount :wallet wallet}))
  (update wallet :balance + amount))

(defn withdraw [wallet amount]
  (cond
    (<= amount 0) (throw+ {:type :wallet/invalid-amount :amount amount :wallet wallet})
    (> amount (available-balance wallet)) (throw+ {:type :wallet/insufficient-balance :wallet wallet :amount amount}))
  (update wallet :balance - amount))

(defn reserve
  "Reserves an order's amount until it's filled, so the amount remains unavailable for future orders."
  [wallet amount]
  (cond
    (<= amount 0) (throw+ {:type :wallet/invalid-amount :amount amount :wallet wallet})
    (> amount (available-balance wallet)) (throw+ {:type :wallet/insufficient-balance :wallet wallet :amount amount}))
  (update wallet :reserved + amount))

(defn cancel-reservation
  "Restores the order's reserved amount."
  [{:keys [reserved] :as wallet} amount]
  (when (> amount reserved)
    (throw+ {:type :wallet/invalid-amount :amount amount :wallet wallet}))
  (update wallet :reserved - amount))

(defn transfer
  "Transfer the `amount` from the `src` wallet to the `dst` wallet.
  Returns both updated wallets."
  [src dst amount]
  (when (not= (:currency src) (:currency dst))
    (throw+ {:type :wallet/different-currencies :src src :dst dst}))
  [(withdraw src amount) (deposit dst amount)])
