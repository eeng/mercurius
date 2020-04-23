(ns fiddles.pub-sub
  (:require [user :refer [system]]
            [mercurius.core.infraestructure.messaging.channel-based-pub-sub :refer [start-channel-based-pub-sub stop-channel-based-pub-sub]]
            [mercurius.core.adapters.messaging.pub-sub :refer [publish subscribe unsubscribe]]))

(comment
  (def bus (start-channel-based-pub-sub))
  (def subscription (subscribe bus "backend.events" {:on-message #(println "backend event:" %)}))
  (subscribe bus "frontend.events" {:on-message #(println "frontend event:" %)})
  (publish bus "backend.events" {:some "msg"})
  (unsubscribe bus subscription)
  (stop-channel-based-pub-sub bus)

  (let [bus (:infraestructure/pub-sub system)]
    (publish bus "push.ticker-updated.BTCUSD" {:ticker "BTCUSD" :last-price 10 :volume 90})))
