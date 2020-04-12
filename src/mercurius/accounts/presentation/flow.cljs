(ns mercurius.accounts.presentation.flow
  (:require [re-frame.core :refer [reg-sub reg-event-fx]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db]]
            [mercurius.core.presentation.api :refer [csrf-token]]
            [day8.re-frame.http-fx]
            [ajax.edn :as edn]))

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

(reg-event-db
 :login-success
 (fn [db [_ {:keys [user]}]]
   (assoc db :auth {:loading? false :user user})))

(reg-event-db
 :login-failure
 (fn [db [_ result]]
   (println "FAILED" result)
   (assoc db :auth {:loading? false :user nil})))

;;;; Subscriptions

(reg-sub
 :logged-in?
 (fn [db _]
   (some? (get-in db [:auth :user]))))

(reg-sub :auth :auth)
