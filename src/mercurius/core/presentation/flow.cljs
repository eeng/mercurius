(ns mercurius.core.presentation.flow
  (:require [re-frame.core :refer [reg-sub reg-event-fx reg-fx]]
            [mercurius.core.presentation.db :refer [default-db]]
            [mercurius.core.presentation.api :refer [send-request]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db >evt]]))

;;;; Effects

(reg-fx
 :api
 (fn [{:keys [request on-success on-failure]}]
   (send-request request
                 :on-success #(>evt (conj on-success %))
                 :on-error #(>evt (conj on-failure %)))))

;;;; Events 

(reg-event-db
 :core/initialize
 (fn [_ [_ _]]
   default-db))

(reg-event-db
 :chsk/state
 (fn [db [_ [_ {:keys [open?]}]]]
   (assoc db :ws-connected? open?)))

(reg-event-db
 :ok-response
 (fn [db [_ db-path result]]
   (assoc-in db db-path {:loading? false :data result})))

(reg-event-fx
 :bad-response
 (fn [{:keys [db]} [_ db-path result]]
   (js/console.error "Backend returned error:" result)
   {:db (assoc-in db db-path {:loading? false :error result})}))

(def domain-event-type-to-reframe
  {:ticker-updated :trading/ticker-updated})

;; Receives push notifications from the backend and routes them to the corresponding re-frame event handler.
(reg-event-fx
 :backend/push
 (fn [_cofx [_ [event-type event-data]]]
   {:dispatch [(domain-event-type-to-reframe event-type) event-data]}))

;;;; Subscriptions

(reg-sub
 :core/initialized?
 (fn [db _]
   (:ws-connected? db)))
