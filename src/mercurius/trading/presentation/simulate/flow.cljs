(ns mercurius.trading.presentation.simulate.flow
  (:require [re-frame.core :refer [reg-sub reg-event-fx]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db]]))

(def default-simulate-form {:loading? false :running? false :progress 0})

;;;; Subscriptions

(reg-sub
 :trading/simulate-form
 (fn [{:keys [simulate-form]}]
   (or simulate-form default-simulate-form)))

;;;; Events

(reg-event-fx
 :trading/start-simulation
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:simulate-form :loading?] true)
    :api {:request [:start-simulation {}]
          :on-success [:command-success [:simulate-form] (assoc (:simulate-form db) :running? true)]
          :on-failure [:command-failure [:simulate-form]]}}))

(reg-event-fx
 :trading/stop-simulation
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:simulate-form :loading?] true)
    :api {:request [:stop-simulation]
          :on-success [:command-success [:simulate-form] (assoc (:simulate-form db) :running? false)]
          :on-failure [:command-failure [:simulate-form]]}}))

(reg-event-db
 :trading/simulation-progress
 (fn [db [_ progress]]
   (if (< progress 1.0)
     (assoc db :simulate-form {:running? true :progress (int (* progress 100))})
     (assoc db :simulate-form {:running? false :progress 0}))))
