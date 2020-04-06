(ns fiddles.pub-sub
  (:require [mercurius.core.adapters.messaging.channel-based-pub-sub :refer [start-channel-based-pub-sub stop-channel-based-pub-sub]]
            [mercurius.core.adapters.messaging.pub-sub :refer [publish subscribe]]))

(comment
  (def bus (start-channel-based-pub-sub))
  (subscribe bus "backend.events" #(println "backend event:" %))
  (subscribe bus "frontend.events" #(println "frontend event:" %))
  (publish bus "backend.events" {:some "event"})
  (stop-channel-based-pub-sub bus))
