(ns mercurius.accounts.presentation.flow
  (:require [re-frame.core :refer [reg-sub reg-event-fx]]))

;;;; Events

(reg-event-fx
 :accounts/login
 (fn [_ [_ credentials]]
   (println "credentials" credentials)
   {}))

;;;; Subscriptions

(reg-sub
 :accounts/logged-in?
 (fn [db _]
   (:logged-in? db)))
