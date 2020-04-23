(ns mercurius.trading.presentation.simulate.flow
  (:require [re-frame.core :refer [reg-sub reg-event-fx]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db]]
            [mercurius.core.presentation.util.string :refer [parse-int]]))

(def default-simulate-form {:loading? false
                            :running? false
                            :progress 0
                            :values {:n-traders 100
                                     :n-orders-per-trader 5
                                     :max-ms-between-orders 1000}})

;;;; Subscriptions

(reg-sub
 :trading/simulate-form
 :simulate-form)

;;;; Events

(reg-event-db
 :trading/simulate-form-changed
 (fn [db [_ form-changes]]
   (update-in db [:simulate-form :values] merge form-changes)))

(defn coerced-command [db]
  (-> (get-in db [:simulate-form :values])
      (update :n-traders parse-int)
      (update :n-orders-per-trader parse-int)
      (update :max-ms-between-orders parse-int)))

(reg-event-fx
 :trading/start-simulation
 (fn [{{:keys [simulate-form] :as db} :db} _]
   {:db (assoc-in db [:simulate-form :loading?] true)
    :api {:request [:start-simulation (coerced-command db)]
          :on-success [:command-success [:simulate-form] (assoc simulate-form :running? true :progress 0)]
          :on-failure [:command-failure [:simulate-form]]}}))

(reg-event-fx
 :trading/stop-simulation
 (fn [{{:keys [simulate-form] :as db} :db} _]
   {:db (assoc-in db [:simulate-form :loading?] true)
    :api {:request [:stop-simulation]
          :on-success [:command-success [:simulate-form] (assoc simulate-form :running? false)]
          :on-failure [:command-failure [:simulate-form]]}}))

(reg-event-fx
 :trading/subscribe-to-simulation-progress
 (fn [_ _]
   {:socket-subscribe {:topic "simulation-progress"
                       :on-message [:trading/simulation-progress]}}))

(reg-event-db
 :trading/simulation-progress
 (fn [db [_ progress]]
   (update db :simulate-form merge
           (if (and (< progress 1.0) (get-in db [:simulate-form :running?]))
             {:progress (int (* progress 100))}
             {:running? false}))))
