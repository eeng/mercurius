(ns mercurius.core.presentation.flow
  (:require [re-frame.core :refer [reg-event-db reg-sub]]))

;;;; Events 

(reg-event-db
 :chsk/state
 (fn [db [_ [_ {:keys [open?]}]]]
   (assoc db :ws-connected? open?)))

;;;; Subscriptions

(reg-sub
 :core/initialized?
 (fn [db _]
   (:ws-connected? db)))
