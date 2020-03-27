(ns mercurius.core.domain.messaging.event-bus
  (:require [tick.alpha.api :as t]
            [mercurius.util.uuid :refer [uuid]]))

(defrecord Event [type id created-at data])

(defn- new-event [type data]
  (map->Event {:type type
               :data data
               :id (uuid)
               :created-at (t/now)}))

(defprotocol EventBus
  (dispatch [this event]
    "Publishes an `Event` to the bus.")

  (subscribe [this event-type out]
    "Allows subscribers to listen to events of a specific type.
    The subscriber must provide a channel where the events will be put."))

(defn publish-event
  "Provides a simplify API to publish events in a simpler form.
  Receives event in the form [event-type event-data], build the event wrapper and dispatches it to the bus."
  [bus [event-type event-data]]
  (->> (new-event event-type event-data)
       (dispatch bus)))
