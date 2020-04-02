(ns mercurius.core.adapters.web.sente
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [clojure.core.async :refer [go-loop <!]]
            [taoensso.timbre :as log]))

(defn- start-router!
  "Receives Sente events and if they correspond to a mediator's request, it's dispatched.
  In addition, if the client provides a reply-fn, it's called with the request's response."
  [{:keys [active ch-recv dispatch]}]
  (go-loop []
    (when @active
      (when-let [{[event-type event-data :as event] :event reply-fn :?reply-fn} (<! ch-recv)]
        (log/trace "Received" event)
        (when (= event-type :backend/request)
          (cond-> (apply dispatch event-data)
            reply-fn reply-fn))
        (recur)))))

(defn start-sente-comms [{:keys [dispatch]}]
  (log/info "Starting Sente comms")
  (let [{:keys [ch-recv send-fn connected-uids
                ajax-post-fn ajax-get-or-ws-handshake-fn]}
        (sente/make-channel-socket! (get-sch-adapter) {})
        active (atom true)]
    (start-router! {:active active
                    :dispatch dispatch
                    :ch-recv ch-recv
                    :chsk-send! send-fn
                    :connected-uids connected-uids})
    {:active active
     :ring-ajax-post ajax-post-fn
     :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn}))

(defn stop-sente-comms [{:keys [active]}]
  (log/info "Stopping Sente comms")
  (reset! active false))
