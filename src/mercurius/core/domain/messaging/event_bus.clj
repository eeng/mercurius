(ns mercurius.core.domain.messaging.event-bus
  (:require [tick.alpha.api :as t]
            [mercurius.util.uuid :refer [uuid]]))

(defrecord Event [type id created-at data])

(defn new-event [type data]
  (map->Event {:type type
               :data data
               :id (uuid)
               :created-at (t/now)}))

(defprotocol EventBus
  "Allows to move domain events through the application."

  (-emit [this event]
    "Publishes an event to the bus. 
    NOTE: For protocols implementers only, clients should use `emit` instead.")

  (listen [this event-type callback]
    "Allows subscribers to listen to events of a specific type.
    The callback will receive instances of `Event`."))

(defn emit [bus [event-type event-data]]
  (->> (new-event event-type event-data)
       (-emit bus)))
