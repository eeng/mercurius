(ns mercurius.core.presentation.api
  (:require [taoensso.sente :as sente]
            [reagent.core :as r]
            [clojure.core.async :refer [go-loop <!]]))

(defonce sente-client (atom nil))
(defonce ws-state (r/atom {:open false}))

(defn- csrf-token []
  (-> js/document (.querySelector "meta[name='csrf-token']") .-content))

(defn connected? []
  (:open? @ws-state))

(defn start-ws-connection-monitor []
  (go-loop []
    (when-let [{[event-type event-data] :event} (<! (:ch-recv @sente-client))]
      (when (= :chsk/state event-type)
        (let [[_ new-state] event-data]
          (reset! ws-state new-state))))))

(defn connect! []
  (when-not (connected?)
    (reset! sente-client (sente/make-channel-socket! "/chsk" (csrf-token) {:type :auto}))
    (start-ws-connection-monitor)))

(def timeout 5000)

(defn chsk-send! [& args]
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

#_(defn use-subscription [event-type]
    (let [result (r/atom nil)
          out-chan (chan)]
      (sub received-events event-type out-chan)
      (go-loop []
        (when-let [{[_ event-data] :event :as event} (<! out-chan)]
          (println "use-sub" event-data)
          (reset! result event-data)))
      result))
