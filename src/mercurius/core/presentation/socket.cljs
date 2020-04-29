(ns mercurius.core.presentation.socket
  "Handles the communication with the backend via Web Sockets."
  (:require [taoensso.sente :as sente]
            [clojure.core.async :refer [go-loop <!]]
            [cljs.core.match :refer-macros [match]]
            [mercurius.core.presentation.util.meta :refer [csrf-token]]))

;; This will hold the Sente client, and a subscriptions map which will allow to register 
;; the frontend callback that will be called everytime a message is pushed from the server.
(defonce sente (atom nil))

(defn- handle-event [event]
  (js/console.log "Received" event)
  (match event

    [:chsk/state [_ {:open? true :uid uid}]]
    (when-let [on-connect (get @sente :on-connect)]
      (on-connect {:uid (when (not= uid :taoensso.sente/nil-uid) uid)}))

    [:chsk/recv [:app/push {:subscription subscription :message message}]]
    (when-let [on-message (get-in @sente [:subscriptions subscription])]
      (on-message message))

    :else nil))

(defn start-events-processor []
  (go-loop []
    (when-let [{event :event} (<! (get-in @sente [:client :ch-recv]))]
      (handle-event event))
    (recur)))

(defn connect! [& {:keys [on-connect]}]
  (reset! sente {:client (sente/make-channel-socket! "/chsk" (csrf-token) {:type :auto})
                 :subscriptions {}
                 :on-connect on-connect})
  (start-events-processor))

(defn reconnect! []
  (sente/chsk-reconnect! (get-in @sente [:client :chsk])))

(def timeout 5000)

(defn- chsk-send! [& args]
  (if @sente
    (apply (get-in @sente [:client :send-fn]) args)
    (throw (js/Error. "Connect to Sente before send!"))))

(defn send-request [request & {:keys [on-success on-error]
                               :or {on-success identity on-error identity}}]
  (chsk-send! [:app/request request]
              timeout
              (fn [reply]
                (if (sente/cb-success? reply)
                  (let [[status data] reply]
                    (case status
                      :ok (on-success data)
                      :error (on-error data)))
                  (js/console.error "Sente reply failure" reply)))))

(defn subscribe [topic {:keys [on-message]}]
  (chsk-send! [:app/subscribe topic]
              timeout
              (fn [reply]
                (if (sente/cb-success? reply)
                  (swap! sente assoc-in [:subscriptions (:subscription reply)] on-message)
                  (js/console.error "Sente reply to :app/subscribe failure" reply)))))
