(ns mercurius.core.presentation.events
  (:require [re-frame.core :refer [reg-event-db]]))

(reg-event-db
 :chsk/state
 (fn [db [_ [_ {:keys [open?]}]]]
   (assoc db :ws-connected? open?)))
