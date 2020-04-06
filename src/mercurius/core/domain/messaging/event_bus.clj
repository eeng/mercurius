(ns mercurius.core.domain.messaging.event-bus
  (:require [tick.alpha.api :as t]
            [clojure.spec.alpha :as s]
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

  (listen [this selector callback]
    "Allows subscribers to listen to events that match the `selector`.
    The selector may be a keyword to match specific event types, 
    or a predicate to do arbitrary
    The callback will receive instances of `Event`."))

(defn emit [bus [event-type event-data]]
  (->> (new-event event-type event-data)
       (-emit bus)))

(s/def ::event (s/cat :type keyword? :data any?))

(s/fdef emit
  :args (s/cat :bus any? :event (s/spec ::event)))
