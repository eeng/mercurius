(ns mercurius.wallets.domain.entities.wallet
  (:require [slingshot.slingshot :refer [throw+]]))

(defn deposit [wallet amount]
  (when (<= amount 0)
    (throw+ {:type ::invalid-deposit-amount}))
  (update wallet :balance + amount))

(defn withdraw [{:keys [balance] :as wallet} amount]
  (cond
    (<= amount 0) (throw+ {:type ::invalid-withdraw-amount})
    (> amount balance) (throw+ {:type ::wallet-overdrawn}))
  (update wallet :balance - amount))
