(ns mercurius.core.presentation.flow
  (:require [re-frame.core :refer [reg-sub reg-event-fx reg-fx]]
            [mercurius.core.presentation.db :refer [default-db]]
            [mercurius.core.presentation.api :as backend]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db >evt]]
            [mercurius.accounts.presentation.flow :refer [assoc-auth]]
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
 (fn [_ [_ _]]
   default-db))

(reg-event-db
 :core/socket-connected
 (fn [db [_ uid]]
   (-> db
       (assoc :ws-connected? true)
       (assoc-auth uid))))

(reg-event-fx
 :ajax-error
 (fn [_ [_ response]]
   (js/console.error "Ajax Error:" response)
   {:toast {:message "Oops! Network issues. Check the logs."
            :type "is-danger faster"}}))

(reg-event-db
 :ok-response
 (fn [db [_ db-path result]]
   (assoc-in db db-path {:loading? false :data result})))

(reg-event-fx
 :bad-response
 (fn [{:keys [db]} [_ db-path result]]
   (js/console.error "Backend Error:" result)
   {:db (assoc-in db db-path {:loading? false :error result})
    :toast {:message (str "Backend Error: " (pr-str result))
            :type "is-danger faster"}}))

(def domain-event-type-to-reframe
  {:ticker-updated :trading/ticker-updated
   :order-book-updated :trading/refresh-order-book
   :trade-made :trading/trade-made})

;; Receives push notifications from the backend and routes them to the corresponding re-frame event handler.
(reg-event-fx
 :backend/push
 (fn [_cofx [_ [be-event-type event-data]]]
   (let [event-type (or (get domain-event-type-to-reframe be-event-type)
                        (throw (js/Error. (str "Backend event not supported: " be-event-type))))]
     {:dispatch [event-type event-data]})))

;;;; Subscriptions

(reg-sub
 :core/initialized?
 (fn [db _]
   (:ws-connected? db)))
