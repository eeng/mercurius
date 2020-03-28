(ns mercurius.trading.domain.entities.trade-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.support.factory :refer [build-order]]
            [mercurius.trading.domain.entities.trade :refer [generate-trade match-orders build-transfers new-trade]]
            [tick.alpha.api :as t]))

(deftest generate-trade-test
  (testing "if the bid price equal to the ask price, a trade is made for that price"
    (let [bid (build-order {:price 100 :amount 5})
          ask (build-order {:price 100 :amount 5})]
      (is (match? {:price 100 :amount 5M} (generate-trade bid ask)))))

  (testing "if the bid price is greater than the ask price, a trade is made for the better price for the first placed order"
    (let [bid (build-order {:price 101 :amount 5 :placed-at (t/time "10:00")})
          ask (build-order {:price 100 :amount 5 :placed-at (t/time "10:05")})]
      (is (match? {:price 100 :amount 5M} (generate-trade bid ask))))

    (let [bid (build-order {:price 101 :amount 5 :placed-at (t/time "10:05")})
          ask (build-order {:price 100 :amount 5 :placed-at (t/time "10:00")})]
      (is (match? {:price 101 :amount 5M} (generate-trade bid ask)))))

  (testing "the amount should be the minimum of the two"
    (let [bid (build-order {:amount 5 :price 100})
          ask (build-order {:amount 6 :price 100})]
      (is (match? {:amount 5M :price 100} (generate-trade bid ask))))
    (let [bid (build-order {:amount 6 :price 100})
          ask (build-order {:amount 5 :price 100})]
      (is (match? {:amount 5M :price 100} (generate-trade bid ask)))))

  (testing "if the order is partially filled, should use the remaining amount"
    (let [bid (build-order {:amount 5 :remaining 4 :price 100})
          ask (build-order {:amount 5 :price 100})]
      (is (match? {:amount 4M :price 100} (generate-trade bid ask))))
    (let [bid (build-order {:amount 5 :price 100})
          ask (build-order {:amount 5 :remaining 3 :price 100})]
      (is (match? {:amount 3M :price 100} (generate-trade bid ask)))))

  (testing "the trade should contain additional data"
    (let [bid (build-order {:price 100 :amount 5 :ticker "BTCUSD"})
          ask (build-order {:price 100 :amount 5 :ticker "BTCUSD"})]
      (is (match? {:ticker "BTCUSD" :bid {:id (:id bid)} :ask {:id (:id ask)}}
                  (generate-trade bid ask)))))

  (testing "if the bid price is lower than the ask price, returns nil"
    (let [bid (build-order {:price 99})
          ask (build-order {:price 100})]
      (is (nil? (generate-trade bid ask)))))

  (testing "if the bid and ask are for the same user, returns nil"
    (let [bid (build-order {:user-id 1 :price 100})
          ask (build-order {:user-id 1 :price 100})]
      (is (nil? (generate-trade bid ask))))))

(deftest match-orders-test
  (testing "if no trade is possible, returns an empty vector"
    (is (= [] (match-orders [] [])))
    (is (= [] (match-orders [(build-order {:price 100})]
                            [(build-order {:price 101})]))))

  (testing "for each pair of partially filled orders, tries to generate a trade"
    (let [[b1 b2] [(build-order {:id "b1" :amount 20}) (build-order {:id "b2" :amount 10})]
          [a1 a2] [(build-order {:id "a1" :amount 15}) (build-order {:id "a2" :amount 20})]
          bids [b1 b2]
          asks [a1 a2]]
      (is (match? [{:amount 15M :bid {:id "b1" :remaining 5M} :ask {:id "a1" :remaining 0M}}
                   {:amount 5M :bid {:id "b1" :remaining 0M} :ask {:id "a2" :remaining 15M}}
                   {:amount 10M :bid {:id "b2" :remaining 0M} :ask {:id "a2" :remaining 5M}}]
                  (match-orders bids asks)))))

  (testing "orders are match by best price first and then by placed-at"
    (let [bids [(build-order {:price 5 :amount 1 :placed-at (t/time "20:01") :id "a"})
                (build-order {:price 5 :amount 1 :placed-at (t/time "20:00") :id "b"})
                (build-order {:price 5 :amount 1 :placed-at (t/time "20:02") :id "c"})
                (build-order {:price 6 :amount 1 :placed-at (t/time "20:03") :id "d"})]
          asks [(build-order {:price 5 :amount 2 :placed-at (t/time "20:30")})]]
      (is (match? [{:bid {:id "d"}}
                   {:bid {:id "b"}}]
                  (match-orders bids asks))))

    (let [bids [(build-order {:price 5 :amount 2 :placed-at (t/time "20:30")})]
          asks [(build-order {:price 5 :amount 1 :placed-at (t/time "20:01") :id "a"})
                (build-order {:price 5 :amount 1 :placed-at (t/time "20:00") :id "b"})
                (build-order {:price 5 :amount 1 :placed-at (t/time "20:02") :id "c"})
                (build-order {:price 4 :amount 1 :placed-at (t/time "20:03") :id "d"})]]
      (is (match? [{:ask {:id "d"}}
                   {:ask {:id "b"}}]
                  (match-orders bids asks))))))

(def buyer 1)
(def seller 2)

(deftest build-transfers-test
  (testing "returns a map with the data necessary to make the transfers corresponding to a trade"
    (let [bid (build-order {:user-id buyer :ticker "BTCUSD"})
          ask (build-order {:user-id seller :ticker "BTCUSD"})
          trade (new-trade {:amount 2 :price 5 :bid bid :ask ask})]
      (is (match? [{:from buyer :to seller :currency "USD" :transfer-amount 10M}
                   {:from seller :to buyer :currency "BTC" :transfer-amount 2M}]
                  (build-transfers trade)))))

  (testing "it also indicates the amounts to cancel reservations"
    (let [bid (build-order {:user-id buyer :ticker "BTCUSD" :price 6})
          ask (build-order {:user-id seller :ticker "BTCUSD" :price 5})
          trade (new-trade {:amount 2 :price 5 :bid bid :ask ask})]
      (is (match? [{:from buyer :to seller :cancel-amount 12M}
                   {:from seller :to buyer :cancel-amount 2M}]
                  (build-transfers trade))))))
