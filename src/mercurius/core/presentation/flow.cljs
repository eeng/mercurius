(ns mercurius.core.presentation.flow
  (:require [re-frame.core :refer [reg-sub reg-event-fx reg-fx]]
            [mercurius.core.presentation.db :refer [default-db]]
            [mercurius.core.presentation.api :refer [send-request]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db >evt]]
            [mercurius.accounts.presentation.flow :refer [assoc-auth]]))

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
 :core/socket-connected
 (fn [db [_ uid]]
   (-> db
       (assoc :ws-connected? true)
       (assoc-auth (some? uid)))))

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
