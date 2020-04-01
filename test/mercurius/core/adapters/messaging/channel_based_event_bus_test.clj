(ns mercurius.core.adapters.messaging.channel-based-event-bus-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.core.async :refer [go chan <! >!!]]
            [matcher-combinators.test]
            [mercurius.support.helpers :refer [with-system recorded-calls]]
            [mercurius.core.domain.messaging.event-bus :refer [publish-event subscribe-to]]))

(deftest event-bus-test
  (testing "subscribe-to returns a channel that delivers the requested event type"
    (with-system [{:adapters/keys [event-bus]} {:only [:adapters/event-bus]}]
      (let [calls (chan)
            events (subscribe-to event-bus :order-placed)]
        (go (>!! calls (<! events)))
        (publish-event event-bus [:trade-made "data2"])
        (publish-event event-bus [:order-placed "data1"])
        (is (match? [{:type :order-placed :data "data1"}]
                    (recorded-calls calls 1))))))

  (testing "subscribing with a callback"
    (with-system [{:adapters/keys [event-bus]} {:only [:adapters/event-bus]}]
      (let [events (chan)
            on-event #(>!! events %)]
        (subscribe-to event-bus :order-placed :on-event on-event)
        (publish-event event-bus [:trade-made "data2"])
        (publish-event event-bus [:order-placed "data1"])
        (is (match? [{:type :order-placed :data "data1"}]
                    (recorded-calls events 1)))))))
