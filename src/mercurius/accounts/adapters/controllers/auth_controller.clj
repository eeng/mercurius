(ns mercurius.accounts.adapters.controllers.auth-controller
  (:require [clojure.core.match :refer [match]]))

(defn login [{:keys [dispatch]} {:keys [body-params session]}]
  (match (dispatch :authenticate body-params)
    [:ok user] {:status 200 :body user :session (assoc session :uid (:id user))}
    [:error error] {:status 401 :body {:error error}}))

(defn logout [{:keys [session]}]
  {:status 200 :session (assoc session :uid nil)})
