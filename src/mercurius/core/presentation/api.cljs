(ns mercurius.core.presentation.api
  (:require [taoensso.sente :as sente]
            [reagent.core :as r]
            [clojure.core.async :refer [go-loop <!]]
            [mercurius.core.presentation.util :refer [>evt]]))

(defonce sente-client (atom nil))

(defn- csrf-token []
  (-> js/document (.querySelector "meta[name='csrf-token']") .-content))

(defn start-events-processor []
  (go-loop []
    (when-let [{[event-type event-data :as event] :event} (<! (:ch-recv @sente-client))]
      (js/console.log event)
      (cond
        (= event-type :chsk/state)
        (>evt event)

        (and (= event-type :chsk/recv)
             (not= event-data [:chsk/ws-ping]))
        (>evt event-data)))
    (recur)))

(defn connect! []
  (reset! sente-client (sente/make-channel-socket! "/chsk" (csrf-token) {:type :auto}))
  (start-events-processor))

(def timeout 5000)

(defn- chsk-send! [& args]
  (if @sente-client
    (apply (:send-fn @sente-client) args)
    (throw (js/Error. "Connect to Sente before send!"))))

(defn send-request [request & {:keys [on-success on-error]
                               :or {on-success identity on-error identity}}]
  (chsk-send! [:backend/request request]
              timeout
              (fn [reply]
                (when (sente/cb-success? reply)
                  (let [[status data] reply]
                    (case status
                      :ok (on-success data)
                      :error (on-error data)))))))

;;;; Higher-level functions

(defn use-query
  "Similar to Apollo GraphQL useQuery.
  Returns a reagent atom that represents the different network request's states: 
  When it's loading, when the response finish successfully, and when an error occours."
  [request]
  (let [result (r/atom {:loading true})]
    (send-request request
                  :on-success #(reset! result {:loading false :data %})
                  :on-error #(reset! result {:loading false :error %}))
    result))
