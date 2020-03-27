(ns fiddles.event-bus
  (:require [mercurius.core.adapters.messaging.channel-based-event-bus :refer [new-channel-based-event-bus]]
            [mercurius.core.domain.messaging.event-bus :refer [publish-event]]))

(comment
  (def bus (new-channel-based-event-bus))
  (publish-event bus [:wallets/deposited {:wallet-id "W1"}])
  (publish-event bus [:trading/trade-made {:trade-id "T1"}])
  (publish-event bus [:trading/trade-made {:trade-id "T2"}]))
