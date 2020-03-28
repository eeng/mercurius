(ns mercurius.trading.adapters.processes.trade-finder-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.core.async :refer [>!! chan]]
            [matcher-combinators.test]
            [matcher-combinators.matchers :as m]
            [mercurius.support.helpers :refer [recorded-calls]]
            [mercurius.support.factory :refer [build-order]]
            [mercurius.core.domain.messaging.event-bus :refer [publish-event]]
            [mercurius.core.adapters.messaging.channel-based-event-bus :refer [new-channel-based-event-bus]]
            [mercurius.trading.adapters.processes.trade-finder :refer [new-trade-finder]]))

(deftest trade-finder-test
  (testing "runs a process that calls the execute-trades use case when an order is placed"
    (with-open [bus (new-channel-based-event-bus)]
      (let [calls (chan)]
        (new-trade-finder {:event-bus bus :execute-trades #(>!! calls %)})
        (publish-event bus [:order-placed (build-order {:ticker "BTCUSD"})])
        (publish-event bus [:order-placed (build-order {:ticker "ETHUSD"})])
        (is (match? (m/in-any-order [{:ticker "BTCUSD"} {:ticker "ETHUSD"}])
                    (recorded-calls calls 2)))))))
