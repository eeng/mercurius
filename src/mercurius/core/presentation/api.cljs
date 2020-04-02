(ns mercurius.core.presentation.api
  (:require [taoensso.sente :as sente]
            [reagent.core :as r]
            [clojure.core.async :refer [go-loop <!]]))

(defn- csrf-token []
  (-> js/document (.querySelector "meta[name='csrf-token']") .-content))

(let [{:keys [send-fn ch-recv]}
      (sente/make-channel-socket! "/chsk" (csrf-token) {:type :auto})]
  (def chsk-send! send-fn)
  (def ch-recv ch-recv))

(def timeout 5000)

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

(defn start-remote-events-processor [on-ws-open]
  (go-loop []
    (when-let [{[event-id event-data] :event} (<! ch-recv)]
      (when (and (= event-id :chsk/state)
                 (-> event-data last :open?))
        (on-ws-open)))))

;;;; Higher level functions

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
