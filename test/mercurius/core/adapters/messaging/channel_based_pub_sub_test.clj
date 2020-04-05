(ns mercurius.core.adapters.messaging.channel-based-pub-sub-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.support.helpers :refer [with-system]]
            [mercurius.core.domain.messaging.pub-sub :refer [publish subscribe]]))

(deftest pub-sub-test
  (testing "on subscriber to a specific topic"
    (with-system [{:adapters/keys [pub-sub]} {:only [:adapters/pub-sub]}]
      (let [topic "some topic"
            calls (atom [])]
        (subscribe pub-sub topic #(swap! calls conj %))
        (publish pub-sub topic "m1")
        (publish pub-sub topic "m2")
        (is (eventually = ["m1" "m"] @calls))))))
