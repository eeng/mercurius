(ns mercurius.core.presentation.flow
  (:require [re-frame.core :refer [reg-event-db reg-sub dispatch]]
            [reagent.ratom :refer [reaction]]
            [mercurius.core.presentation.api :refer [send-request]]))

;;;; Events 

(reg-event-db
 :chsk/state
 (fn [db [_ [_ {:keys [open?]}]]]
   (assoc db :ws-connected? open?)))

(reg-event-db
 :write-to
 (fn [db [_ path data]]
   (assoc-in db path data)))

;;;; Subscriptions

(defn remote-query-sub [app-db request db-path]
  (send-request request
                :on-success #(dispatch [:write-to db-path {:loading? false :data %}])
                :on-error #(dispatch [:write-to db-path {:loading? false :error %}]))
  (reaction (get-in @app-db db-path {:loading? true})))

(reg-sub
 :core/initialized?
 (fn [db _]
   (:ws-connected? db)))
