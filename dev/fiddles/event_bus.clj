(ns fiddles.event-bus
  (:require [mercurius.core.adapters.messaging.channel-based-event-bus :refer [new-channel-based-event-bus]]
            [mercurius.core.domain.messaging.event-bus :refer [publish-event subscribe-to]]
            [clojure.core.async :refer [go-loop <!]]))

(comment
  (def bus (new-channel-based-event-bus))

  (let [out (subscribe-to bus :trade-made)]
    (go-loop []
      (if-let [ev (<! out)]
        (do
          (println "Received" ev)
          (recur))
        (println "Bye!"))))

  (publish-event bus [:trade-made "trade data"])
  (publish-event bus [:order-placed "order data"])

  (.close bus))
