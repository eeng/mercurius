(ns mercurius.wallets.domain.entities.wallet
  (:require [slingshot.slingshot :refer [throw+]]
            [mercurius.util.uuid :refer [uuid]]
            [clojure.spec.alpha :as s]))

(def available-currencies #{"USD" "EUR" "BTC" "ETH"})

(s/def :wallet/currency available-currencies)

(defrecord Wallet [id user-id currency balance reserved])

(defn new-wallet [{:keys [user-id currency balance reserved] :or {balance 0 reserved 0}}]
  (s/assert :wallet/currency currency)
  (map->Wallet {:id (uuid)
                :user-id user-id
                :currency currency
                :balance balance
                :reserved reserved}))

(defn- available-balance [{:keys [balance reserved]}]
  (- balance reserved))

(defn deposit [wallet amount]
  (when (<= amount 0)
    (throw+ {:type :wallet/invalid-amount :amount amount}))
  (update wallet :balance + amount))

(defn withdraw [wallet amount]
  (cond
    (<= amount 0) (throw+ {:type :wallet/invalid-amount :amount amount})
    (> amount (available-balance wallet)) (throw+ {:type :wallet/insufficient-balance :wallet wallet :amount amount}))
  (update wallet :balance - amount))

(defn reserve
  "Reserves an order's amount until it's filled, so the amount remains unavailable for future orders."
  [wallet amount]
  (cond
    (<= amount 0) (throw+ {:type :wallet/invalid-amount :amount amount})
    (> amount (available-balance wallet)) (throw+ {:type :wallet/insufficient-balance :wallet wallet :amount amount}))
  (update wallet :reserved + amount))

(defn transfer
  "Transfer the `amount` from the `src` wallet to the `dst` wallet.
  Returns both updated wallets."
  [src dst amount]
  (when (not= (:currency src) (:currency dst))
    (throw+ {:type :wallet/different-currencies :src src :dst dst}))
  [(withdraw src amount) (deposit dst amount)])
