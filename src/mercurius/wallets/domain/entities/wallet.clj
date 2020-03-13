(ns mercurius.wallets.domain.entities.wallet
  (:require [slingshot.slingshot :refer [throw+]]
            [mercurius.lib.uuid :refer [uuid]]))

(defn new-wallet [user-id currency]
  {:id (uuid) :user-id user-id :currency currency :balance 0})

(defn deposit [wallet amount]
  (when (<= amount 0)
    (throw+ {:type ::invalid-deposit-amount}))
  (update wallet :balance + amount))

(defn withdraw [{:keys [balance] :as wallet} amount]
  (cond
    (<= amount 0) (throw+ {:type ::invalid-withdraw-amount})
    (> amount balance) (throw+ {:type ::wallet-overdrawn}))
  (update wallet :balance - amount))
