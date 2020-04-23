(ns mercurius.core.adapters.messaging.pub-sub-event-bus
  (:require [mercurius.core.domain.messaging.event-bus :refer [EventBus]]
            [mercurius.core.adapters.messaging.pub-sub :refer [publish subscribe]]))

(def events-ns "domain-events.")

(defn- event-type-to-topic [event-type]
  (str events-ns (.-sym event-type)))

(defrecord PubSubEventBus [pub-sub]
  EventBus

  (-emit [_ {:keys [type] :as event}]
    (publish pub-sub (event-type-to-topic type) event))

  (listen [_ selector callback]
    (if (keyword? selector)
      (subscribe pub-sub (event-type-to-topic selector) callback)
      (subscribe pub-sub (str events-ns "*") (fn [{:keys [type] :as event}]
                                               (when (selector type)
                                                 (callback event)))))))

(defn new-pub-sub-event-bus [{:keys [pub-sub]}]
  (PubSubEventBus. pub-sub))
