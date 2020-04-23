(ns mercurius.core.infraestructure.web.sente
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [clojure.core.async :refer [go-loop <!]]
            [taoensso.timbre :as log]
            [mercurius.core.adapters.messaging.pub-sub :refer [subscribe]]
            [mercurius.util.uuid :refer [uuid]]))

(defn- route-to-controller
  "Routes a request (which comes from the frontend on the Sente event's data) to the respective controller.
  A Sente controller should be a function that takes the request and a context map, and returns an :ok/:error tuple."
  [{[_ event-data] :event reply-fn :?reply-fn :keys [uid]}
   {:keys [use-case-controller simulation-controller]}]
  (let [[request-type _] event-data
        controller (condp some [request-type]
                     #{:start-simulation :stop-simulation} simulation-controller
                     use-case-controller)
        context {:user-id uid}]
    (when-let [response (controller event-data context)]
      (reply-fn response))))

(defn- handle-subscribe [{[_ topic] :event reply-fn :?reply-fn :keys [uid]}
                         send-fn
                         {:keys [pub-sub]}]
  (let [client-subscription-id (uuid) ; TODO instead of this we could return the topic itself so when reloading it overrides the subscription and we don't need to clear-subscriptions
        on-message #(send-fn uid [:app/push {:subscription client-subscription-id :message %}])
        topic (str "push." topic) ; Scope the push notifications to prevent clashes with domain events
        ;; Here we are allowing clients to subscribe to any topics. 
        ;; In a real scenario we would make some kind of authorization check.
        ;; TODO use map this to uid so when it disconnect we unsubscribe
        ;; FIXME when reloading another subscription it's created. Must implement some way to override a subscription
        server-subscription (subscribe pub-sub topic on-message)]
    (reply-fn {:subscription client-subscription-id})))

(defn- start-frontend-request-router
  "Receives events from the clients and dispatch the ones that are requests to the processor. 
  It ignores pings an other Sente events."
  [active ch-recv send-fn deps]
  (go-loop []
    (when @active
      (when-let [{[event-type _] :event :as event-msg} (<! ch-recv)]
        (case event-type
          ;; TODO rename to :app
          :frontend/request
          (route-to-controller event-msg deps)

          :app/subscribe
          (handle-subscribe event-msg send-fn deps)

          nil)
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

    (start-frontend-request-router active ch-recv send-fn deps)
    (start-backend-event-pusher active send-fn connected-uids pub-sub)

    {:active active
     :ring-ajax-post ajax-post-fn
     :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn}))

(defn stop-sente [{:keys [active]}]
  (log/info "Stopping Sente")
  (reset! active false))
