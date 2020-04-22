(ns fiddles.pub-sub
  (:require [user :refer [system]]
            [mercurius.core.infraestructure.messaging.channel-based-pub-sub :refer [start-channel-based-pub-sub stop-channel-based-pub-sub]]
            [mercurius.core.adapters.messaging.pub-sub :refer [publish subscribe]]
            [mercurius.util.uuid :refer [uuid]]
            [tick.alpha.api :as t]))

(comment
  (def bus (start-channel-based-pub-sub))
  (subscribe bus "backend.events" #(println "backend event:" %))
  (subscribe bus "frontend.events" #(println "frontend event:" %))
  (stop-channel-based-pub-sub bus)

  (let [bus (:infraestructure/pub-sub system)]
    (publish bus "push.ticker-updated.BTCUSD" [:ticker-updated {:ticker "BTCUSD" :last-price 10 :volume 90}])
    (publish bus "push.trade-made.BTCUSD" [:trade-made {:id (uuid) :ticker "BTCUSD" :price 10 :amount 90 :created-at (t/now)}])))
