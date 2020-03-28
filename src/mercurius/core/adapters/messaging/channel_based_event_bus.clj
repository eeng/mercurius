(ns mercurius.core.adapters.messaging.channel-based-event-bus
  (:require [mercurius.core.domain.messaging.event-bus :refer [EventBus]]
            [clojure.core.async :refer [put! chan pub sub close!]]))

(defrecord ChannelBasedEventBus [in-chan events-pub]
  EventBus

  (dispatch [_ event]
    (put! in-chan event))

  (subscribe [_ event-type out-chan]
    (sub events-pub event-type out-chan))

  java.io.Closeable

  (close [_]
    (close! in-chan)))

(defn new-channel-based-event-bus []
  (let [in-chan (chan)]
    (ChannelBasedEventBus. in-chan (pub in-chan :type))))
