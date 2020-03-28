(ns mercurius.core.domain.messaging.event-bus
  (:require [clojure.spec.alpha :as s]
            [tick.alpha.api :as t]
            [mercurius.util.uuid :refer [uuid]]
            [clojure.core.async :refer [chan]]))

(defrecord Event [type id created-at data])

(defn- new-event [type data]
  (map->Event {:type type
               :data data
               :id (uuid)
               :created-at (t/now)}))

(defprotocol EventBus
  (dispatch [this event]
    "Publishes an `Event` to the bus.
    NOTE: Low-level function, clients should use `publish-event` instead.")

  (subscribe [this event-type out-chan]
    "Allows subscribers to listen to events of a specific type.
    Events will be delivered to the output channel.
    NOTE: Low-level function, clients should use `subscribe-to` instead."))

(defn publish-event
  "Publishes an event to the bus.
  The event must be in the form [event-type event-data]. 
  It builds the event wrapper and dispatches it to the bus."
  [bus [event-type event-data]]
  (s/assert ::event-type event-type)
  (->> (new-event event-type event-data)
       (dispatch bus)))

(defn subscribe-to
  "Allows to subscribe to specific event types."
  [bus event-type & {:keys [out-chan] :or {out-chan (chan)}}]
  (subscribe bus event-type out-chan)
  out-chan)

(s/def ::event-type #{:order-placed :trade-made})
