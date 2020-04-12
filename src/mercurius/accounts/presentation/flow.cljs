(ns mercurius.accounts.presentation.flow
  (:require [re-frame.core :refer [reg-sub reg-event-fx]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db]]
            [mercurius.core.presentation.backend :refer [csrf-token]]
            [day8.re-frame.http-fx]
            [ajax.edn :as edn]))

(defn assoc-auth [db user-id]
  (assoc db :auth {:loading? false :user-id user-id}))

;;;; Events

(reg-event-fx
 :login
 (fn [{:keys [db]} [_ credentials]]
   {:db (assoc db :auth {:loading? true})
    :http-xhrio {:method :post
                 :uri "/login"
                 :params credentials
                 :on-success [:login-success]
                 :on-failure [:login-failure]
                 :headers {"X-CSRF-Token" (csrf-token)}
                 :timeout 5000
                 :format (edn/edn-request-format)
                 :response-format (edn/edn-response-format)}}))

(reg-event-fx
 :login-success
 (fn [_ _]
   ;; We need to reconnect the ws so Sente picks up the new uid. 
   ;; When it's done, the :core/socket-connected event handler will update the auth status on the db.
   {:api {:reconnect true}}))

(reg-event-db
 :login-failure
 (fn [db [_ result]]
   ;; TODO handle login failure
   (println "FAILED" result)
   (assoc-auth db nil)))

(reg-event-fx
 :logout
 (fn [_ _]
   {:http-xhrio {:method :post
                 :uri "/logout"
                 :on-success [:logout-success]
                 :on-failure [:logout-failure]
                 :headers {"X-CSRF-Token" (csrf-token)}
                 :timeout 5000
                 :format (edn/edn-request-format)
                 :response-format (edn/edn-response-format)}}))

(reg-event-db
 :logout-success
 (fn [db [_ _]]
   (assoc-auth db nil)))

(reg-event-db
 :logout-failure
 (fn [db [_ result]]
   ;; TODO handle logout failure
   (println "LOGOUT failed" result)
   db))

;;;; Subscriptions

(reg-sub
 :logged-in?
 (fn [db _]
   (some? (get-in db [:auth :user-id]))))

(reg-sub :auth :auth)
