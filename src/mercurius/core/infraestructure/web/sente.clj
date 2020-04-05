(ns mercurius.core.infraestructure.web.sente
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [clojure.core.async :refer [go-loop <!]]
            [taoensso.timbre :as log]))

(defn- start-frontend-request-router [active ch-recv request-processor]
  (go-loop []
    (when @active
      (when-let [{[event-type event-data] :event reply-fn :?reply-fn} (<! ch-recv)]
        (when (= event-type :frontend/request)
          (cond-> (request-processor event-data)
            reply-fn reply-fn))
        (recur)))))

(defn start-sente [{:keys [request-processor]}]
  (log/info "Starting Sente")
  (let [{:keys [ch-recv send-fn connected-uids
                ajax-post-fn ajax-get-or-ws-handshake-fn]}
        (sente/make-channel-socket! (get-sch-adapter) {})
        active (atom true)]

    (start-frontend-request-router active ch-recv request-processor)

    {:active active
     :ring-ajax-post ajax-post-fn
     :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn}))

(defn stop-sente [{:keys [active]}]
  (log/info "Stopping Sente")
  (reset! active false))
