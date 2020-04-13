(ns mercurius.core.presentation.api
  "Handles the communication with the backend via Web Sockets."
  (:require [taoensso.sente :as sente]
            [clojure.core.async :refer [go-loop <!]]
            [cljs.core.match :refer-macros [match]]
            [mercurius.core.presentation.util.reframe :refer [>evt]]))

(defonce sente-client (atom nil))

(defn csrf-token []
  (-> js/document (.querySelector "meta[name='csrf-token']") .-content))

(defn- handle-event [event]
  (js/console.log "Received" event)
  (match event
    [:chsk/state [_ {:open? true :uid uid}]]
    (>evt [:core/socket-connected (when (not= uid :taoensso.sente/nil-uid) uid)])

    [:chsk/recv ([:backend/push _] :as backend-event)]
    (>evt backend-event)

    :else nil))

(defn start-events-processor []
  (go-loop []
    (when-let [{event :event} (<! (:ch-recv @sente-client))]
      (handle-event event))
    (recur)))

(defn connect! []
  (reset! sente-client (sente/make-channel-socket! "/chsk" (csrf-token) {:type :auto}))
  (start-events-processor))

(defn reconnect! []
  (sente/chsk-reconnect! (:chsk @sente-client)))

(def timeout 5000)

(defn- chsk-send! [& args]
  (if @sente-client
    (apply (:send-fn @sente-client) args)
    (throw (js/Error. "Connect to Sente before send!"))))

(defn send-request [request & {:keys [on-success on-error]
                               :or {on-success identity on-error identity}}]
  (chsk-send! [:frontend/request request]
              timeout
              (fn [reply]
                (when (sente/cb-success? reply)
                  (let [[status data] reply]
                    (case status
                      :ok (on-success data)
                      :error (on-error data)))))))
