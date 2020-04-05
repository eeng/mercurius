(ns mercurius.core.adapters.web.sente
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [clojure.core.async :refer [go-loop <!]]
            [taoensso.timbre :as log]
            [mercurius.core.adapters.controllers.pub-sub :refer [PubSub]]))

(defrecord Sente [ring-ajax-post ring-ajax-get-or-ws-handshake
                  active ch-recv chsk-send! connected-uids]
  PubSub

  ;; This implementation is a bit naive, only works for one subscriber!
  ;; But that's all I need for know.
  (subscribe [_ msg-type callback]
    (go-loop []
      (when @active
        (when-let [{[event-type :as event] :event reply-fn :?reply-fn} (<! ch-recv)]
          (when (= event-type msg-type)
            (cond-> (callback event)
              reply-fn reply-fn))
          (recur)))))

  (broadcast! [_ msg]
    (doseq [uid (:any @connected-uids)]
      (chsk-send! uid msg))))

(defn start-sente []
  (log/info "Starting Sente")
  (let [{:keys [ch-recv send-fn connected-uids
                ajax-post-fn ajax-get-or-ws-handshake-fn]}
        (sente/make-channel-socket! (get-sch-adapter) {})]
    (map->Sente
     {:active (atom true)
      :ring-ajax-post ajax-post-fn
      :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn
      :ch-recv ch-recv
      :chsk-send! send-fn
      :connected-uids connected-uids})))

(defn stop-sente [{:keys [active]}]
  (log/info "Stopping Sente")
  (reset! active false))
