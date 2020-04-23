(ns mercurius.core.infraestructure.web.sente
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [clojure.core.async :refer [go-loop <!]]
            [taoensso.timbre :as log]
            [mercurius.core.adapters.messaging.pub-sub :refer [subscribe]]))

(defn- route-to-controller
  "Routes a request (which comes from the frontend on the Sente event's data) to the respective controller.
  A Sente controller should be a function that takes the request and a context map, and returns an :ok/:error tuple."
  [{[_ event-data] :event :keys [uid]}
   {:keys [use-case-controller simulation-controller]}]
  (let [[request-type _] event-data
        controller (condp some [request-type]
                     #{:start-simulation :stop-simulation} simulation-controller
                     use-case-controller)
        context {:user-id uid}]
    (controller event-data context)))

(defn- start-frontend-request-router
  "Receives events from the clients and dispatch the ones that are requests to the processor. 
  It ignores pings an other Sente events."
  [active ch-recv deps]
  (go-loop []
    (when @active
      (when-let [{[event-type _ :as e] :event reply-fn :?reply-fn :as event-msg} (<! ch-recv)]
        (when (= event-type :frontend/request)
          (let [response (route-to-controller event-msg deps)]
            (cond-> response
              reply-fn reply-fn)))
        (recur)))))

(defn- start-backend-event-pusher
  "Subscribes to the topic 'push.*' and pushes those messages to all the clients.
  In a real-life application we'd have clients join specific topics,
  and then here we would notify only those interested."
  [active send-fn connected-uids pub-sub]
  (go-loop []
    (when @active
      (subscribe pub-sub
                 "push.*"
                 (fn [message]
                   (doseq [uid (:any @connected-uids)]
                     (send-fn uid [:backend/push message])))))))

(defn start-sente [{:keys [pub-sub] :as deps}]
  (log/info "Starting Sente")
  (let [{:keys [ch-recv send-fn connected-uids
                ajax-post-fn ajax-get-or-ws-handshake-fn]}
        (sente/make-channel-socket! (get-sch-adapter) {})
        active (atom true)]

    (start-frontend-request-router active ch-recv deps)
    (start-backend-event-pusher active send-fn connected-uids pub-sub)

    {:active active
     :ring-ajax-post ajax-post-fn
     :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn}))

(defn stop-sente [{:keys [active]}]
  (log/info "Stopping Sente")
  (reset! active false))
