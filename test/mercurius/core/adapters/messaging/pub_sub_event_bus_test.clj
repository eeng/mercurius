(ns mercurius.core.adapters.messaging.pub-sub-event-bus-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.support.helpers :refer [with-system]]
            [mercurius.core.domain.messaging.event-bus :refer [emit listen]]))

(deftest pub-sub-event-bus-test
  (testing "allows to listen with a keyword selector"
    (with-system [{:adapters/keys [event-bus]} {:only [:adapters/event-bus]}]
      (let [evs (atom [])]
        (listen event-bus :events/e1 #(swap! evs conj %))
        (emit event-bus [:events/e2 "ignored"])
        (emit event-bus [:events/e1 "e1"])
        (is (match-eventually? [{:type :events/e1 :data "e1"}] evs)))))

  (testing "allows to listen with a predicate selector"
    (with-system [{:adapters/keys [event-bus]} {:only [:adapters/event-bus]}]
      (let [evs (atom [])]
        (listen event-bus #{:events/e1 :events/e2} #(swap! evs conj %))
        (emit event-bus [:events/e1 "e1"])
        (emit event-bus [:events/ignored "ignored"])
        (emit event-bus [:events/e2 "e2"])
        (is (match-eventually? [{:type :events/e1 :data "e1"} {:type :events/e2 :data "e2"}] evs))))))
