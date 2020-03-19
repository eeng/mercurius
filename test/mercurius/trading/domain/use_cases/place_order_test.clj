(ns mercurius.trading.domain.use-cases.place-order-test
  (:require [clojure.test :refer [deftest testing is]]
            [spy.core :as spy]
            [spy.assert :as assert]
            [mercurius.support.asserts :refer [submap?]]
            [mercurius.support.factory :refer [build-wallet]]
            [mercurius.trading.domain.use-cases.place-order :refer [new-place-order-use-case]]))

(deftest execute-test
  (testing "for a buy order, should reserve the corresponding amount in the last currency's wallet"
    (let [wallet (build-wallet {:balance 50 :currency "USD"})
          fetch-wallet (spy/mock (constantly wallet))
          save-wallet (spy/mock identity)
          place-order (new-place-order-use-case {:fetch-wallet fetch-wallet
                                                 :save-wallet save-wallet
                                                 :insert-order identity})]
      (place-order {:user-id 1 :type :limit :side :buy :amount 0.2 :ticker "BTCUSD" :price 100})
      (assert/called-with? fetch-wallet 1 "USD")
      (assert/called-with? save-wallet (assoc wallet :reserved (* 0.2 100)))))

  (testing "for a sell order, should reserve the amount in the first currency's wallet"
    (let [wallet (build-wallet {:balance 1 :currency "BTC"})
          fetch-wallet (spy/mock (constantly wallet))
          save-wallet (spy/mock identity)
          place-order (new-place-order-use-case {:fetch-wallet fetch-wallet
                                                 :save-wallet save-wallet
                                                 :insert-order identity})]
      (place-order {:user-id 1 :type :limit :side :sell :amount 0.2 :ticker "BTCUSD" :price 100})
      (assert/called-with? fetch-wallet 1 "BTC")
      (assert/called-with? save-wallet (assoc wallet :reserved 0.2))))

  (testing "should insert the order in the order book"
    (let [wallet (build-wallet {:balance 50 :currency "USD"})
          insert-order (spy/mock identity)
          place-order (new-place-order-use-case {:fetch-wallet (constantly wallet)
                                                 :save-wallet identity
                                                 :insert-order insert-order})]
      (place-order {:user-id 1 :type :limit :side :buy :amount 0.1 :ticker "BTCUSD" :price 100})
      (let [[[order]] (spy/calls insert-order)]
        (is (submap? {:side :buy :amount 0.1 :price 100} order))
        (is (some? (:id order)))
        (is (some? (:placed-at order)))))))
