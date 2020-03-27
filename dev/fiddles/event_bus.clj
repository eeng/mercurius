(ns fiddles.event-bus
  (:require [mercurius.core.adapters.messaging.channel-based-event-bus :refer [new-channel-based-event-bus]]
            [mercurius.core.domain.messaging.event-bus :refer [publish-event subscribe]]
            [clojure.core.async :refer [go-loop <!]]))

(comment
  (def bus (new-channel-based-event-bus))

  (let [out (subscribe bus :trading/trade-made)]
    (go-loop []
      (if-let [ev (<! out)]
        (do
          (println "Received" ev)
          (recur))
        (println "Bye!"))))

  (publish-event bus [:trading/trade-made {:trade-id "T1"}])
  (publish-event bus [:wallets/deposited {:wallet-id "W1"}])

  (.close bus))
