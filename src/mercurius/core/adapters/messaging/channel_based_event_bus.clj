(ns mercurius.core.adapters.messaging.channel-based-event-bus
  (:require [mercurius.core.domain.messaging.event-bus :refer [EventBus]]
            [clojure.core.async :refer [put! chan pub sub close!]]
            [taoensso.timbre :as log]))

(defrecord ChannelBasedEventBus [in-chan events-pub]
  EventBus

  (dispatch [_ event]
    (put! in-chan event))

  (subscribe [_ event-type out-chan]
    (sub events-pub event-type out-chan)))

(defn start-channel-based-event-bus []
  (log/info "Starting channel based event bus")
  (let [in-chan (chan)]
    (ChannelBasedEventBus. in-chan (pub in-chan :type))))

(defn stop-channel-based-event-bus [{:keys [in-chan]}]
  (log/info "Stopping channel based event bus")
  (close! in-chan))
