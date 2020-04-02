(ns mercurius.core.adapters.web.handler
  (:require [reitit.ring :as ring]))

(defn status [_req]
  {:status 200 :body "System is online!"})

(defn router [dispatch]
  (ring/router
   [["/status" {:get status}]
    ["/system" {:get (fn [_req] {:status 200 :body (dispatch :get-tickers)})}]]))

(defn handler [dispatch]
  (ring/ring-handler (router dispatch)
                     (ring/routes
                      (ring/create-resource-handler {:path "/"})
                      (ring/create-default-handler))))

(comment
  ((handler identity) {:request-method :get :uri "/"}))
