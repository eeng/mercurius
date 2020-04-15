(ns mercurius.wallets.domain.entities.wallet
  (:require [clojure.spec.alpha :as s]
            [slingshot.slingshot :refer [throw+]]
            [mercurius.util.uuid :refer [uuid]]
            [mercurius.util.money :refer [money]]))

(def available-currencies #{"USD" "EUR" "BTC" "ETH"})

(s/def ::currency available-currencies)
(s/def ::bigdec #(instance? BigDecimal %))
(s/def ::balance ::bigdec)
(s/def ::reserved ::bigdec)
(s/def ::wallet (s/keys :req-un [::currency ::balance ::reserved]))

(defrecord Wallet [id user-id currency balance reserved last-events])

(defn new-wallet [{:keys [id user-id currency balance reserved]
                   :or {id (uuid) balance 0 reserved 0}}]
  {:pre [(s/assert ::currency currency)]
   :post [(s/assert ::wallet %)]}
  (map->Wallet {:id id
                :user-id user-id
                :currency currency
                :balance (money balance)
                :reserved (money reserved)}))

(defn available-balance [{:keys [balance reserved]}]
  (- balance reserved))

(defn- trx-event [event-type wallet extra-data]
  (let [event-data (-> wallet
                       (select-keys [:user-id :currency :balance :reserved])
                       (merge extra-data))]
    [event-type event-data]))

(defn- add-event [wallet event-type extra-data]
  (let [event (trx-event event-type wallet extra-data)]
    (update wallet :last-events (fnil conj []) event)))

(defn deposit [wallet amount]
  (s/assert ::wallet wallet)
  (when (<= amount 0)
    (throw+ {:type :wallet/invalid-amount :amount amount :wallet wallet}))
  (-> (update wallet :balance + (money amount))
      (add-event :deposited-into-wallet {:amount amount})))

(defn withdraw [wallet amount]
  (s/assert ::wallet wallet)
  (cond
    (<= amount 0) (throw+ {:type :wallet/invalid-amount :amount amount :wallet wallet})
    (> amount (available-balance wallet)) (throw+ {:type :wallet/insufficient-balance :wallet wallet :amount amount}))
  (-> (update wallet :balance - (money amount))
      (add-event :withdraw-from-wallet {:amount amount})))

(defn reserve
  "Reserves an order's amount until it's filled, so the amount remains unavailable for future orders."
  [wallet amount]
  (s/assert ::wallet wallet)
  (cond
    (<= amount 0) (throw+ {:type :wallet/invalid-amount :amount amount :wallet wallet})
    (> amount (available-balance wallet)) (throw+ {:type :wallet/insufficient-balance :wallet wallet :amount amount}))
  (-> (update wallet :reserved + (money amount))
      (add-event :reserve-from-wallet {:amount amount})))

(defn cancel-reservation
  "Restores the order's reserved amount."
  [{:keys [reserved] :as wallet} amount]
  (when (> amount reserved)
    (throw+ {:type :wallet/invalid-amount :amount amount :wallet wallet}))
  (-> (update wallet :reserved - (money amount))
      (add-event :cancel-wallet-reserve {:amount amount})))

(defn transfer
  "Transfer the `amount` from the `src` wallet to the `dst` wallet.
  Returns both updated wallets."
  [src dst amount]
  (s/assert ::wallet src)
  (s/assert ::wallet dst)
  (when (not= (:currency src) (:currency dst))
    (throw+ {:type :wallet/different-currencies :src src :dst dst}))
  (when (= (:id src) (:id dst))
    (throw+ {:type :wallet/transfer-between-same :src src :dst dst}))
  [(withdraw src amount) (deposit dst amount)])
