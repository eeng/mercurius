(ns mercurius.core.presentation.flux
  (:require [re-frame.core :refer [reg-sub reg-event-fx reg-fx]]
            [mercurius.core.presentation.db :refer [default-db]]
            [mercurius.core.presentation.socket :as socket]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db >evt]]
            ["bulma-toast" :refer [toast]]))

;;;; Effects

(reg-fx
 :socket-request
 (fn [{:keys [request on-success on-failure]}]
   (socket/send-request request
                        :on-success #(>evt (conj on-success %))
                        :on-error #(>evt (conj on-failure %)))))

(reg-fx
 :socket-subscribe
 (fn [configs]
   (let [configs (if (map? configs) [configs] configs)]
     (doseq [{:keys [topic on-message]} configs]
       (socket/subscribe topic {:on-message #(>evt (conj on-message %))})))))

(reg-fx
 :socket-reconnect
 (fn [_]
   (socket/reconnect!)))

(reg-fx
 :toast
 (fn [opts]
   (let [defaults {:position "bottom-right"
                   :duration 4000
                   :dismissible true
                   :animate {:in "fadeInUp" :out "fadeOutRight"}}]
     (toast (clj->js (merge defaults opts))))))

;;;; Events 

(reg-event-db
 :core/initialize
 (fn [_ _]
   default-db))

(reg-event-db
 :core/reset-db
 (fn [{:keys [ws-connected?]} _]
   (assoc default-db :ws-connected? ws-connected?)))

(reg-event-db
 :core/socket-connected
 (fn [db [_ uid]]
   (assoc db :ws-connected? true :auth {:user-id uid})))

(reg-event-fx
 :ajax-error
 (fn [_ [_ response]]
   (js/console.error "Ajax Error:" response)
   {:toast {:message "Oops! Network issues. Check the logs."
            :type "is-danger faster"
            :duration 5000}}))

(reg-event-db
 :query-success
 (fn [db [_ db-path result]]
   (assoc-in db db-path {:loading? false :data result})))

(reg-event-fx
 :query-failure
 (fn [{:keys [db]} [_ db-path result]]
   (js/console.error "Backend Error:" result)
   {:db (assoc-in db db-path {:loading? false :error result})
    :toast {:message (str "Backend Error: " (pr-str result))
            :type "is-danger faster"
            :duration 5000}}))

(reg-event-fx
 :command-success
 (fn [{:keys [db]} [_ db-path data-to-store msg]]
   (cond-> {:db (assoc-in db db-path data-to-store)}
     msg (assoc :toast {:message msg
                        :type "is-success faster"
                        :duration 4000}))))

(reg-event-fx
 :command-failure
 (fn [{:keys [db]} [_ db-path error]]
   (js/console.error error)
   {:db (assoc-in db (conj db-path :loading?) false)
    :toast {:message (case (:type error)
                       :wallet/insufficient-balance "Insufficient balance."
                       "Unexpected error.")
            :type "is-danger faster"}}))

;;;; Subscriptions

(reg-sub
 :core/initialized?
 (fn [db _]
   (:ws-connected? db)))
