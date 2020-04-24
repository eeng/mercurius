(ns mercurius.trading.adapters.processes.trade-finder-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.support.helpers :refer [with-system]]
            [mercurius.support.factory :refer [build-order]]
            [mercurius.core.domain.messaging.event-bus :refer [emit]]
            [mercurius.trading.adapters.processes.trade-finder :refer [new-trade-finder]]))

(deftest trade-finder-test
  (testing "runs a process that calls the execute-trades use case when an order is placed"
    (with-system [{:adapters/keys [event-bus]} {:only [:adapters/event-bus]}]
      (let [evs (atom [])]
        (new-trade-finder {:event-bus event-bus :execute-trades #(swap! evs conj %)})
        (emit event-bus [:order-placed (build-order {:ticker "BTCUSD"})])
        (emit event-bus [:order-placed (build-order {:ticker "ETHUSD"})])
        (is (match-eventually? [{:ticker "BTCUSD"} {:ticker "ETHUSD"}] evs))))))
