(ns fiddles.event-bus
  (:require [mercurius.core.adapters.messaging.channel-based-event-bus :refer [start-channel-based-event-bus stop-channel-based-event-bus]]
            [mercurius.core.domain.messaging.event-bus :refer [publish-event subscribe-to]]
            [clojure.core.async :refer [go-loop <!]]))

(comment
  (def bus (start-channel-based-event-bus))

  (let [out (subscribe-to bus :trade-made)]
    (go-loop []
      (if-let [ev (<! out)]
        (do
          (println "Received" ev)
          (recur))
        (println "Bye!"))))

  (publish-event bus [:trade-made "trade data"])
  (publish-event bus [:order-placed "order data"])

  (stop-channel-based-event-bus bus))
