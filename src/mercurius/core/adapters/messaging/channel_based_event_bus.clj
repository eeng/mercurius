(ns mercurius.core.adapters.messaging.channel-based-event-bus
  (:require [mercurius.core.domain.messaging.event-bus :refer [EventBus]]
            [clojure.core.async :refer [put! chan]]))

(defrecord ChannelBasedEventBus [bus]
  EventBus

  (dispatch [_ event]
    (put! bus event)))

(defn new-channel-based-event-bus []
  (ChannelBasedEventBus. (chan)))
