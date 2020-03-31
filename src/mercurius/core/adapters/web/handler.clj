(ns mercurius.core.adapters.web.handler
  (:require [reitit.ring :as ring]))

(defn status [_req]
  {:status 200 :body "ok"})

(defn router [dispatch]
  (ring/router
   [["/status" {:get status}]
    ["/system" {:get (fn [_req] {:status 200 :body (dispatch :get-tickers)})}]]))

(defn handler [dispatch]
  (ring/ring-handler (router dispatch)))
