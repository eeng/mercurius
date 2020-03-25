(ns mercurius.trading.adapters.presenters.order-book-summary-test
  (:require [clojure.test :refer [deftest testing is]]
            [mercurius.support.factory :refer [build-order]]
            [matcher-combinators.test]
            [mercurius.trading.adapters.presenters.order-book-summary :refer [summarize-order-book round-with-pow-of-ten precision-to-decimal-places]]))

(deftest round-with-pow-of-ten-test
  (testing "rounding with 10^-1"
    (is (= 51.1 (round-with-pow-of-ten 51.16 0.1)))
    (is (= 51.1 (round-with-pow-of-ten 51.11 0.1))))

  (testing "rounding with 10^0"
    (is (= 173.0 (round-with-pow-of-ten 173.4 1)))
    (is (= 51.0 (round-with-pow-of-ten 51.1 1)))
    (is (= 51.0 (round-with-pow-of-ten 51.9 1)))
    (is (= 9.0 (round-with-pow-of-ten 9.55 1)))
    (is (= 0.0 (round-with-pow-of-ten 0.3 1))))

  (testing "rounding with 10^1"
    (is (= 170.0 (round-with-pow-of-ten 173.4 10)))
    (is (= 50.0 (round-with-pow-of-ten 51.1 10)))
    (is (= 50.0 (round-with-pow-of-ten 51.9 10)))
    (is (= 0.0 (round-with-pow-of-ten 9.55 10)))
    (is (= 0.0 (round-with-pow-of-ten 0.3 10)))))

(deftest precision-to-decimal-places-test
  (testing "returns the divisor corresponding to the precision given some max-price"
    (let [max-price 9999.9]
      (is (= 1 (precision-to-decimal-places "P0" max-price)))
      (is (= 0 (precision-to-decimal-places "P1" max-price)))
      (is (= -1 (precision-to-decimal-places "P2" max-price)))
      (is (= -2 (precision-to-decimal-places "P3" max-price)))
      (is (= -3 (precision-to-decimal-places "P4" max-price))))

    (let [max-price 999.9]
      (is (= 2 (precision-to-decimal-places "P0" max-price)))
      (is (= 1 (precision-to-decimal-places "P1" max-price)))
      (is (= 0 (precision-to-decimal-places "P2" max-price)))
      (is (= -1 (precision-to-decimal-places "P3" max-price)))
      (is (= -2 (precision-to-decimal-places "P4" max-price))))

    (let [max-price 0.09]
      (is (= 6 (precision-to-decimal-places "P0" max-price)))
      (is (= 5 (precision-to-decimal-places "P1" max-price)))
      (is (= 4 (precision-to-decimal-places "P2" max-price)))
      (is (= 3 (precision-to-decimal-places "P3" max-price)))
      (is (= 2 (precision-to-decimal-places "P4" max-price))))))

(deftest summarize-order-book-test
  (testing "groups the orders by price rounded to the specified divisor"
    (let [order-book {:buying [(build-order {:amount 0.2 :price 53.3})
                               (build-order {:amount 0.4 :price 53.4})
                               (build-order {:amount 1.1 :price 52.1})
                               (build-order {:amount 2.1 :price 44.7})]
                      :selling [(build-order {:amount 0.4 :price 61.4})]}]
      (is (match? {:buying [{:count 2 :amount 0.6M :price 53.0}
                            {:count 1 :amount 1.1M :price 52.0}
                            {:count 1 :amount 2.1M :price 45.0}]
                   :selling [{:count 1 :amount 0.4M :price 61.0}]}
                  (summarize-order-book order-book {:precision "P3"})))
      (is (match? {:buying [{:count 3 :amount 1.7M :price 50.0}
                            {:count 1 :amount 2.1M :price 40.0}]
                   :selling [{:count 1 :amount 0.4M :price 60.0}]}
                  (summarize-order-book order-book {:precision "P4"})))))

  (testing "summarizes the orders remaining amount"
    (let [order-book {:buying [(build-order {:amount 3 :remaining 2 :price 53.3})
                               (build-order {:amount 4 :price 53.7})]
                      :selling []}]
      (is (match? {:buying [{:count 2 :amount 6M :price 50.0}]
                   :selling []}
                  (summarize-order-book order-book {:precision "P4"})))))

  (testing "when the order book is empty"
    (let [order-book {:buying [] :selling []}]
      (is (= order-book (summarize-order-book order-book {:precision "P0"}))))))
