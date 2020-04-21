(ns mercurius.trading.presentation.simulate.flow
  (:require [re-frame.core :refer [reg-sub reg-event-fx]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db]]
            [mercurius.core.presentation.util.string :refer [parse-float]]))

;;;; Subscriptions

(reg-sub
 :trading/simulate-form
 (fn [{:keys [simulate-form]}]
   simulate-form))


;;;; Events

(reg-event-fx
 :trading/start-simulation
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:simulate-form :loading?] true)
    :api {:request [:start-simulation {}]
          :on-success [:command-success [:simulate-form] {:running? true :loading? false}]
          :on-failure [:command-failure [:simulate-form]]}}))

(reg-event-fx
 :trading/stop-simulation
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:simulate-form :loading?] true)
    :api {:request [:stop-simulation]
          :on-success [:command-success [:simulate-form] {:running? false :loading? false}]
          :on-failure [:command-failure [:simulate-form]]}}))
