(ns mercurius.trading.domain.entities.trade-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.support.factory :refer [build-order]]
            [mercurius.trading.domain.entities.trade :refer [generate-trade]]
            [tick.alpha.api :as t]))

(deftest test-generate-trade
  (testing "if the bid price equal to the ask price, a trade is made for that price"
    (let [bid (build-order {:price 100 :amount 5})
          ask (build-order {:price 100 :amount 5})]
      (is (match? {:price 100 :amount 5} (generate-trade bid ask)))))

  (testing "if the bid price is greater than the ask price, a trade is made for the better price for the first placed order"
    (let [bid (build-order {:price 101 :amount 5 :placed-at (t/time "10:00")})
          ask (build-order {:price 100 :amount 5 :placed-at (t/time "10:05")})]
      (is (match? {:price 100 :amount 5} (generate-trade bid ask))))

    (let [bid (build-order {:price 101 :amount 5 :placed-at (t/time "10:05")})
          ask (build-order {:price 100 :amount 5 :placed-at (t/time "10:00")})]
      (is (match? {:price 101 :amount 5} (generate-trade bid ask)))))

  (testing "the amount should be the minimum of the two"
    (let [bid (build-order {:price 100 :amount 5})
          ask (build-order {:price 100 :amount 6})]
      (is (match? {:price 100 :amount 5} (generate-trade bid ask))))
    (let [bid (build-order {:price 100 :amount 6})
          ask (build-order {:price 100 :amount 5})]
      (is (match? {:price 100 :amount 5} (generate-trade bid ask)))))

  (testing "the trade should contain additional data"
    (let [bid (build-order {:price 100 :amount 5 :ticker "BTCUSD"})
          ask (build-order {:price 100 :amount 5 :ticker "BTCUSD"})]
      (is (match? {:ticker "BTCUSD" :created-at some? :bid bid :ask ask}
                  (generate-trade bid ask)))))

  (testing "if the bid price is lower than the ask price, returns nil"
    (let [bid (build-order {:price 99})
          ask (build-order {:price 100})]
      (is (nil? (generate-trade bid ask))))))
