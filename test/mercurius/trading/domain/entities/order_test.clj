(ns mercurius.trading.domain.entities.order-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [spy.core :as spy]
            [spy.assert :as assert]
            [mercurius.support.factory :refer [build-order]]
            [mercurius.trading.domain.entities.order :refer [currency-paid calculate-reservation calculate-price]]))

(deftest new-order-test
  (testing "should round the amount to 5 decimal places"
    (is (match? {:amount 0.11111M} (build-order {:amount 0.11111111333})))))

(deftest currency-paid-test
  (testing "returns the last currency when buying and the first one when selling"
    (is (= "USD" (currency-paid :buy "BTCUSD")))
    (is (= "BTC" (currency-paid :sell "BTCUSD")))
    (is (= "ETH" (currency-paid :sell "ETHUSD")))))

(deftest calculate-reservation-test
  (testing "for a buy order, should reserve the corresponding amount in the last currency's wallet"
    (let [order (build-order {:side :buy :amount 0.2 :price 100 :ticker "BTCUSD"})]
      (is (match? {:amount (* 0.2M 100) :currency "USD"}
                  (calculate-reservation order)))))

  (testing "for a sell order, should reserve the corresponding amount in the last currency's wallet"
    (let [order (build-order {:side :sell :amount 0.2 :price 100 :ticker "BTCUSD"})]
      (is (match? {:amount 0.2M :currency "BTC"}
                  (calculate-reservation order))))))

(deftest calculate-price-test
  (testing "for a limit order, the order already has its priced"
    (let [get-bid-ask (spy/spy)
          place-order-command {:type :limit :side :buy :price 10M}]
      (is (= 10M (calculate-price place-order-command get-bid-ask)))
      (assert/not-called? get-bid-ask)))

  (testing "for a buy market order, it should assign the ask price"
    (let [get-bid-ask (spy/mock (constantly {:bid 10M :ask 11M}))]
      (is (= 11M (calculate-price {:ticker "BTCUSD" :type :market :side :buy} get-bid-ask)))
      (is (= 10M (calculate-price {:ticker "BTCUSD" :type :market :side :sell} get-bid-ask)))
      (assert/called-with? get-bid-ask "BTCUSD"))))
