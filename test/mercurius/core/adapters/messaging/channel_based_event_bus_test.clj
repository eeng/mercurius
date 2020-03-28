(ns mercurius.core.adapters.messaging.channel-based-event-bus-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.core.domain.messaging.event-bus :refer [publish-event subscribe-to]]
            [mercurius.core.adapters.messaging.channel-based-event-bus :refer [new-channel-based-event-bus]]
            [clojure.core.async :refer [go chan timeout <! >!! alts!!]]))

(deftest event-bus-test
  (testing "subscribe-to returns a channel that delivers the requested event type"
    (with-open [bus (new-channel-based-event-bus)]
      (let [events (subscribe-to bus :order-placed)
            go-block (go
                       (let [ev (<! events)]
                         (is (match? {:type :order-placed :data "data1"} ev))))]
        (publish-event bus [:trade-made "data2"])
        (publish-event bus [:order-placed "data1"])
        (alts!! [go-block (timeout 100)]))))

  (testing "subscribing with a callback"
    (with-open [bus (new-channel-based-event-bus)]
      (let [events (chan)
            on-event #(>!! events %)]
        (subscribe-to bus :order-placed :on-event on-event)
        (publish-event bus [:trade-made "data2"])
        (publish-event bus [:order-placed "data1"])
        (is (match? {:type :order-placed :data "data1"}
                    (first (alts!! [events (timeout 100)]))))))))
