(ns mercurius.core.infraestructure.web.sente
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [clojure.core.async :refer [go-loop <!]]
            [taoensso.timbre :as log]
            [mercurius.core.adapters.messaging.pub-sub :refer [subscribe unsubscribe]]))

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

;; Here we are allowing clients to subscribe to any topics. 
;; In a real scenario we would make some kind of authorization check.
(defn- handle-subscribe
  [{[_ topic] :event reply-fn :?reply-fn :keys [uid]}
   send-fn
   {:keys [pub-sub]}]
  (let [subscription-id (str uid ":" topic) ; This id makes the subscribe idempotent, so when the frontend reloads, or components remount, they don't need to unsubscribe.
        on-message #(send-fn uid [:app/push {:subscription subscription-id :message %}])
        scoped-topic (str "push." topic)] ; Scope the push notifications to prevent clashes with domain events
    (subscribe pub-sub scoped-topic {:on-message on-message :subscription-id subscription-id})
    (reply-fn {:subscription subscription-id})))

(defn- handle-unsubscribe [{[_ subscriptions] :event} {:keys [pub-sub]}]
  (doseq [subscription subscriptions]
    (unsubscribe pub-sub subscription)))

(defn- start-frontend-request-router
  "Receives events from the clients and dispatch the ones that are requests to the processor. 
  It ignores pings an other Sente events."
  [active ch-recv send-fn deps]
  (go-loop []
    (when @active
      (when-let [{[event-type _] :event :as event-msg} (<! ch-recv)]
        (case event-type
          ;; TODO rename to :app
          :app/request
          (route-to-controller event-msg deps)

          :app/subscribe
          (handle-subscribe event-msg send-fn deps)

          :app/unsubscribe
          (handle-unsubscribe event-msg deps)

          nil)
        (recur)))))

(defn start-sente [deps]
  (log/info "Starting Sente")
  (let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn]}
        (sente/make-channel-socket! (get-sch-adapter) {})
        active (atom true)]
    (start-frontend-request-router active ch-recv send-fn deps)
    {:active active
     :ring-ajax-post ajax-post-fn
     :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn}))

(defn stop-sente [{:keys [active]}]
  (log/info "Stopping Sente")
  (reset! active false))
