(ns mercurius.core.infraestructure.messaging.channel-based-pub-sub-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.core.async :refer [chan >!!]]
            [matcher-combinators.test]
            [matcher-combinators.matchers :as m]
            [mercurius.support.helpers :refer [with-system recorded-calls]]
            [mercurius.core.adapters.messaging.pub-sub :refer [publish subscribe]]
            [mercurius.core.infraestructure.messaging.channel-based-pub-sub :refer [match-topic?]]))

(deftest pub-sub-test
  (testing "one subscriber to a specific topic"
    (with-system [{pub-sub :infraestructure/pub-sub} {:only [:infraestructure/pub-sub]}]
      (let [calls (chan)]
        (subscribe pub-sub "t1" {:on-message #(>!! calls %)})
        (publish pub-sub "t1" "m1")
        (publish pub-sub "t2" "ignored")
        (publish pub-sub "t1" "m2")
        (is (match? ["m1" "m2"] (recorded-calls calls 2))))))

  (testing "multiple subscribers to the same specific topic"
    (with-system [{pub-sub :infraestructure/pub-sub} {:only [:infraestructure/pub-sub]}]
      (let [topic "some topic"
            calls (chan)]
        (subscribe pub-sub topic {:on-message #(>!! calls ["s1" %])})
        (subscribe pub-sub topic {:on-message #(>!! calls ["s2" %])})
        (publish pub-sub topic "msg")
        (is (match? (m/in-any-order [["s1" "msg"] ["s2" "msg"]])
                    (recorded-calls calls 2))))))

  (testing "subscribing with a pattern"
    (with-system [{pub-sub :infraestructure/pub-sub} {:only [:infraestructure/pub-sub]}]
      (let [calls (chan)]
        (subscribe pub-sub "events.*" {:on-message #(>!! calls %)})
        (publish pub-sub "events.orders" "m1")
        (publish pub-sub "other" "ignored")
        (publish pub-sub "events.tickers" "m2")
        (is (match? ["m1" "m2"] (recorded-calls calls 2)))))))

(deftest match-topic?-test
  (testing "without wildcards"
    (is (match-topic? "room.1" "room.1"))
    (is (not (match-topic? "room.1" "room.2")))
    (is (not (match-topic? "room" "room.2"))))

  (testing "with wildcards"
    (is (match-topic? "room.*" "room.1"))
    (is (not (match-topic? "room.*" "rooming")))))
