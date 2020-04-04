(ns mercurius.core.presentation.flow
  (:require [re-frame.core :refer [reg-sub]]
            [reagent.ratom :refer [reaction]]
            [mercurius.core.presentation.api :refer [send-request]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db >evt]]))

;;;; Events 

(reg-event-db
 :core/initialize
 (fn [_ [_ _]]
   {:order-book-precision "P0"}))

(reg-event-db
 :chsk/state
 (fn [db [_ [_ {:keys [open?]}]]]
   (assoc db :ws-connected? open?)))

(reg-event-db
 :write-to
 (fn [db [_ path data]]
   (assoc-in db path data)))

;;;; Subscriptions

(defn remote-query-sub
  "Generic subscription to retrieve data from the backend.
  A map is stored at `db-path` representing the different network states."
  [app-db request db-path]
  (send-request request
                :on-success #(>evt [:write-to db-path {:loading? false :data %}])
                :on-error #(>evt [:write-to db-path {:loading? false :error %}]))
  (reaction (get-in @app-db db-path {:loading? true})))

(reg-sub
 :core/initialized?
 (fn [db _]
   (:ws-connected? db)))
