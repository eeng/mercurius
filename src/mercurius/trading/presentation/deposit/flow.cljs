(ns mercurius.trading.presentation.deposit.flow
  (:require [re-frame.core :refer [reg-sub reg-event-fx]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db]]
            [mercurius.core.presentation.util.string :refer [parse-float]]))

(def default-deposit-form
  {:values {:amount "" :currency "USD"}
   :loading? false
   :valid? false})

;;;; Subscriptions

(defn- coerced-command [db]
  (-> (get-in db [:deposit-form :values])
      (update :amount parse-float)))

(reg-sub
 :trading/deposit-form
 (fn [{:keys [deposit-form] :as db}]
   (assoc deposit-form
          :valid? (-> (coerced-command db) :amount pos?))))

;;;; Events

(reg-event-db
 :trading/deposit-form-changed
 (fn [db [_ form-changes]]
   (update-in db [:deposit-form :values] merge form-changes)))

(reg-event-fx
 :trading/deposit
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:deposit-form :loading?] true)
    :socket-request
    {:request [:deposit (coerced-command db)]
     :on-success [:command-success [:deposit-form] default-deposit-form "Deposit successful!"]
     :on-failure [:command-failure [:deposit-form]]}}))
