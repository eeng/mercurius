(ns fiddles.event-bus
  (:require [mercurius.core.domain.messaging.event-bus :refer [emit listen]]
            [user :refer [system]]))

(comment
  (let [bus (:adapters/event-bus system)]
    (listen bus :ticker-updated println)
    (emit bus [:ticker-updated {:ticker "BTCUSD" :last-price 100M :volume 90M}])))
