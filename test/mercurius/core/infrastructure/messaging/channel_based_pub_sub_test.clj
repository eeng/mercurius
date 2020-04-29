(ns mercurius.core.infrastructure.messaging.channel-based-pub-sub-test
  (:require [clojure.test :refer [deftest testing is]]
            [mercurius.support.helpers :refer [with-system]]
            [mercurius.core.adapters.messaging.pub-sub :refer [publish subscribe]]
            [mercurius.core.infrastructure.messaging.channel-based-pub-sub :refer [match-topic?]]))

(deftest pub-sub-test
  (testing "one subscriber to a specific topic"
    (with-system [{pub-sub :infrastructure/pub-sub} {:only [:infrastructure/pub-sub]}]
      (let [msgs (atom [])]
        (subscribe pub-sub "t1" {:on-message #(swap! msgs conj %)})
        (publish pub-sub "t1" "m1")
        (publish pub-sub "t2" "ignored")
        (publish pub-sub "t1" "m2")
        (is (match-eventually? ["m1" "m2"] msgs)))))

  (testing "multiple subscribers to the same specific topic"
    (with-system [{pub-sub :infrastructure/pub-sub} {:only [:infrastructure/pub-sub]}]
      (let [topic "some topic"
            msgs (atom [])]
        (subscribe pub-sub topic {:on-message #(swap! msgs conj {:sub 1 :msg %})})
        (subscribe pub-sub topic {:on-message #(swap! msgs conj {:sub 2 :msg %})})
        (publish pub-sub topic "msg")
        (is (match-eventually? [{:sub 1 :msg "msg"} {:sub 2 :msg "msg"}] msgs)))))

  (testing "subscribing with a pattern"
    (with-system [{pub-sub :infrastructure/pub-sub} {:only [:infrastructure/pub-sub]}]
      (let [msgs (atom [])]
        (subscribe pub-sub "events.*" {:on-message #(swap! msgs conj %)})
        (publish pub-sub "events.orders" "m1")
        (publish pub-sub "other" "ignored")
        (publish pub-sub "events.tickers" "m2")
        (is (match-eventually? ["m1" "m2"] msgs))))))

(deftest match-topic?-test
  (testing "without wildcards"
    (is (match-topic? "room.1" "room.1"))
    (is (not (match-topic? "room.1" "room.2")))
    (is (not (match-topic? "room" "room.2"))))

  (testing "with wildcards"
    (is (match-topic? "room.*" "room.1"))
    (is (not (match-topic? "room.*" "rooming")))))
