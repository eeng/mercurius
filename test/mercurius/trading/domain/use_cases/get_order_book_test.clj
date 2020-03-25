(ns mercurius.trading.domain.use-cases.get-order-book-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [spy.core :as spy]
            [spy.assert :as assert]
            [mercurius.support.factory :refer [build-order]]
            [mercurius.trading.domain.use-cases.get-order-book :refer [new-get-order-book-use-case]]))

(deftest get-order-book-test
  (testing "getting the raw order book"
    (let [the-book {:buying [] :selling []}
          repo-fn (spy/mock (constantly the-book))
          get-order-book (new-get-order-book-use-case {:get-order-book repo-fn})]
      (is (= the-book (get-order-book {:ticker "BTCUSD"})))
      (assert/called-with? repo-fn "BTCUSD")))

  (testing "getting a summarized order book"
    (let [order-book {:buying [(build-order {:price 99})]}
          get-order-book (new-get-order-book-use-case {:get-order-book (constantly order-book)})]
      (is (match? {:buying [{:count 1 :price 90.0}]}
                  (get-order-book {:ticker "BTCUSD" :precision "P4"}))))))
