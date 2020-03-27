(ns mercurius.core.adapters.messaging.channel-based-event-bus-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.core.domain.messaging.event-bus :refer [publish-event subscribe]]
            [mercurius.core.adapters.messaging.channel-based-event-bus :refer [new-channel-based-event-bus]]
            [clojure.core.async :refer [go timeout <! alts!!]]))

(deftest event-bus-test
  (testing "subscribe returns a channel that delivers the requested event type"
    (with-open [bus (new-channel-based-event-bus)]
      (let [events (subscribe bus :ev1)
            go-block (go
                       (let [ev (<! events)]
                         (is (match? {:type :ev1 :data "data1"} ev))))]
        (publish-event bus [:ev2 "data2"])
        (publish-event bus [:ev1 "data1"])
        (alts!! [go-block (timeout 100)])))))
