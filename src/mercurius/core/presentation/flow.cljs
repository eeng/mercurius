(ns mercurius.core.presentation.flow
  (:require [re-frame.core :refer [reg-sub reg-event-fx reg-fx]]
            [mercurius.core.presentation.db :refer [default-db]]
            [mercurius.core.presentation.api :as backend]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db >evt]]
            ["bulma-toast" :refer [toast]]))

;;;; Effects

(reg-fx
 :api
 (fn [{:keys [request on-success on-failure reconnect]}]
   (if reconnect
     (backend/reconnect!)
     (backend/send-request request
                           :on-success #(>evt (conj on-success %))
                           :on-error #(>evt (conj on-failure %))))))

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

(def domain-event-type-to-reframe
  {:ticker-updated :trading/ticker-updated
   :order-book-updated :trading/refresh-order-book
   :trade-executed :trading/trade-executed
   :wallet-changed :trading/wallet-changed})

;; Receives push notifications from the backend and routes them to the corresponding re-frame event handler.
;; *1) In a real application we would have the client subscribe to the corresponding topic to receive more targeted notifications.
(reg-event-fx
 :backend/push
 (fn [{:keys [db]} [_ [be-event-type event-data]]]
   (let [event-type (or (get domain-event-type-to-reframe be-event-type)
                        (throw (js/Error. (str "Backend event not supported: " be-event-type))))
         user-id (get-in db [:auth :user-id])]
     ;; This checks is needed in order to filter events not targeted to the user.
     ;; If *1 is implementented we wouldn't need it.
     (when (or (not (:user-id event-data))
               (= (:user-id event-data) user-id))
       {:dispatch [event-type event-data]}))))

;;;; Subscriptions

(reg-sub
 :core/initialized?
 (fn [db _]
   (:ws-connected? db)))
