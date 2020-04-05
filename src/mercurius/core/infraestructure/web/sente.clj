(ns mercurius.core.infraestructure.web.sente
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [clojure.core.async :refer [go-loop <!]]
            [taoensso.timbre :as log]
            [mercurius.core.domain.messaging.event-bus :refer [subscribe-to]]))

(defn- start-frontend-request-router
  "Receives events from the clients and dispatch the ones that are requests to the processor. 
  It ignores pings an other Sente events."
  [active ch-recv request-processor]
  (go-loop []
    (when @active
      (when-let [{[event-type event-data] :event reply-fn :?reply-fn} (<! ch-recv)]
        (when (= event-type :frontend/request)
          (cond-> (request-processor event-data)
            reply-fn reply-fn))
        (recur)))))

(defn- start-backend-event-notifier
  "Listens to domain events and notifies them to the clients.
  In a complex application we'll have to make clients join specific topics,
  and then here notify only those interested."
  [active send-fn connected-uids event-bus]
  (go-loop []
    (when @active
      ; TODO this is notifying only one event, it should work for all and not refer to them
      (subscribe-to event-bus
                    :ticker-updated
                    :on-event (fn [{:keys [data]}]
                                (doseq [uid (:any @connected-uids)]
                                  (send-fn uid [:trading/ticker-updated data])))))))

(defn start-sente [{:keys [request-processor event-bus]}]
  (log/info "Starting Sente")
  (let [{:keys [ch-recv send-fn connected-uids
                ajax-post-fn ajax-get-or-ws-handshake-fn]}
        (sente/make-channel-socket! (get-sch-adapter) {})
        active (atom true)]

    (start-frontend-request-router active ch-recv request-processor)
    (start-backend-event-notifier active send-fn connected-uids event-bus)

    {:active active
     :ring-ajax-post ajax-post-fn
     :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn}))

(defn stop-sente [{:keys [active]}]
  (log/info "Stopping Sente")
  (reset! active false))
