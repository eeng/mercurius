(ns mercurius.core.infraestructure.web.sente
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [clojure.core.async :refer [go-loop <!]]
            [taoensso.timbre :as log]
            [mercurius.core.adapters.messaging.pub-sub :refer [subscribe]]))

(defn- start-frontend-request-router
  "Receives events from the clients and dispatch the ones that are requests to the processor. 
  It ignores pings an other Sente events."
  [active ch-recv request-processor]
  (go-loop []
    (when @active
      (when-let [{[event-type event-data] :event reply-fn :?reply-fn :keys [uid]} (<! ch-recv)]
        (when (= event-type :frontend/request)
          (println "SENTE uid" uid)
          (cond-> (request-processor event-data)
            reply-fn reply-fn))
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

(defn start-sente [{:keys [request-processor pub-sub]}]
  (log/info "Starting Sente")
  (let [{:keys [ch-recv send-fn connected-uids
                ajax-post-fn ajax-get-or-ws-handshake-fn]}
        (sente/make-channel-socket! (get-sch-adapter) {})
        active (atom true)]

    (start-frontend-request-router active ch-recv request-processor)
    (start-backend-event-pusher active send-fn connected-uids pub-sub)

    {:active active
     :ring-ajax-post ajax-post-fn
     :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn}))

(defn stop-sente [{:keys [active]}]
  (log/info "Stopping Sente")
  (reset! active false))
