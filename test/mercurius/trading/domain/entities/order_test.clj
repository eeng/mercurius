(ns mercurius.trading.domain.entities.order-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.support.factory :refer [build-order]]
            [mercurius.trading.domain.entities.order :refer [currency-paid]]))

(deftest new-order-test
  (testing "should round the amount to 5 decimal places"
    (is (match? {:amount 0.11111M} (build-order {:amount 0.11111111333})))))

(deftest currency-paid-test
  (testing "returns the last currency when buying and the first one when selling"
    (is (= "USD" (currency-paid :buy "BTCUSD")))
    (is (= "BTC" (currency-paid :sell "BTCUSD")))
    (is (= "ETH" (currency-paid :sell "ETHUSD")))))
