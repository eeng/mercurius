(ns mercurius.accounts.presentation.login.flow
  (:require [re-frame.core :refer [reg-sub reg-event-fx]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db]]
            [mercurius.core.presentation.util.meta :refer [csrf-token]]
            [day8.re-frame.http-fx]
            [ajax.edn :as edn]))

(def default-login-form {:loading? false :values {:username "" :password ""}})

;;;; Subscriptions

(reg-sub
 :logged-in?
 (fn [db _]
   (some? (get-in db [:auth :user-id]))))

(reg-sub :login-form :login-form)

;;;; Events

(reg-event-db
 :login-form-changed
 (fn [db [_ changes]]
   (update-in db [:login-form :values] merge changes)))

(reg-event-fx
 :login
 (fn [{:keys [db]} [_]]
   {:db (assoc-in db [:login-form :loading?] true)
    :http-xhrio {:method :post
                 :uri "/login"
                 :params (get-in db [:login-form :values])
                 :on-success [:login-success]
                 :on-failure [:login-failure]
                 :headers {"X-CSRF-Token" (csrf-token)}
                 :timeout 5000
                 :format (edn/edn-request-format)
                 :response-format (edn/edn-response-format)}}))

(reg-event-fx
 :login-success
 (fn [{:keys [db]} _]
   ;; We need to reconnect the ws so Sente picks up the new uid. 
   ;; When it's done, the :core/socket-connected event handler will update the auth status on the db.
   {:socket-reconnect true
    :db (assoc db :login-form default-login-form)}))

(reg-event-fx
 :login-failure
 (fn [{:keys [db]} [_ response]]
   {:db (assoc-in db [:login-form :loading?] false)
    :toast {:message (case (:status response)
                       401 "Invalid username or password."
                       "There seems to be a network issue.")
            :type "is-danger faster"
            :duration 3000}}))

(reg-event-fx
 :logout
 (fn [_ _]
   {:http-xhrio {:method :post
                 :uri "/logout"
                 :on-success [:core/reset-db]
                 :on-failure [:ajax-error]
                 :headers {"X-CSRF-Token" (csrf-token)}
                 :timeout 5000
                 :format (edn/edn-request-format)
                 :response-format (edn/edn-response-format)}}))
