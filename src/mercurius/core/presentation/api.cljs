(ns mercurius.core.presentation.api
  "Handles the communication with the backend via Web Sockets."
  (:require [taoensso.sente :as sente]
            [clojure.core.async :refer [go-loop <!]]
            [cljs.core.match :refer-macros [match]]
            [mercurius.core.presentation.util.reframe :refer [>evt]]))

;; This will hold the Sente client, and a subscriptions map which will allow to register 
;; the frontend callback that will be called everytime a message is pushed from the server.
(defonce sente (atom nil))

(defn csrf-token []
  (-> js/document (.querySelector "meta[name='csrf-token']") .-content))

(defn- handle-event [event]
  (js/console.log "Received" event)
  (match event
    [:chsk/state [_ {:open? true :uid uid}]]
    (>evt [:core/socket-connected (when (not= uid :taoensso.sente/nil-uid) uid)])

    ;; TODO remove
    [:chsk/recv ([:backend/push _] :as backend-event)]
    (>evt backend-event)

    [:chsk/recv [:app/push {:subscription subscription :message message}]]
    (when-let [on-message (get-in @sente [:subscriptions subscription])]
      (on-message message))

    :else nil))

(defn start-events-processor []
  (go-loop []
    (when-let [{event :event} (<! (get-in @sente [:client :ch-recv]))]
      (handle-event event))
    (recur)))

;; TODO Remove the dependency from trom re-frame in this ns to make it more general. 
;; Instead receive an on-connect callback here
(defn connect! []
  (reset! sente {:client (sente/make-channel-socket! "/chsk" (csrf-token) {:type :auto})
                 :subscriptions {}})
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
  (chsk-send! [:frontend/request request]
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

(defn clear-subscriptions! []
  (swap! sente assoc :subscriptions {}))

(js/setTimeout #(subscribe "ticker-updated.*" {:on-message println}) 500)
#_(js/setTimeout #(println (:subscriptions @sente)) 1000)
