(ns mercurius.trading.domain.use-cases.match-orders-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [spy.core :as spy]
            [spy.assert :as assert]
            [mercurius.support.factory :refer [build-wallet build-order]]
            [mercurius.trading.domain.use-cases.match-orders :refer [new-match-orders-use-case]]))

(deftest match-orders-test
  (testing "if the orders match should make two wallet transfers (one for each currency)"
    (let [[buyer seller] [1 2]
          buyer-usd-wallet (build-wallet {:balance 110 :currency "USD" :user-id buyer})
          seller-usd-wallet (build-wallet {:balance 7 :currency "USD" :user-id seller})
          buyer-btc-wallet (build-wallet {:balance 2 :currency "BTC" :user-id buyer})
          seller-btc-wallet (build-wallet {:balance 5 :currency "BTC" :user-id seller})
          fetch-wallet (spy/mock (fn [user-id currency]
                                   (case [user-id currency]
                                     [1 "USD"] buyer-usd-wallet
                                     [2 "BTC"] seller-btc-wallet)))
          load-wallet (spy/mock (fn [user-id currency]
                                  (case [user-id currency]
                                    [1 "BTC"] buyer-btc-wallet
                                    [2 "USD"] seller-usd-wallet)))
          save-wallet (spy/mock identity)
          match-orders (new-match-orders-use-case {:fetch-wallet fetch-wallet
                                                   :load-wallet load-wallet
                                                   :save-wallet save-wallet})
          bid (build-order {:price 50 :amount 2 :ticker "BTCUSD" :user-id buyer})
          ask (build-order {:price 50 :amount 2 :ticker "BTCUSD" :user-id seller})]
      (is (match? {:price 50} (match-orders {:bid bid :ask ask})))
      (let [[[buyer-usd-wallet] [seller-usd-wallet] [seller-btc-wallet] [buyer-btc-wallet]]
            (spy/calls save-wallet)]
        (is (match? {:balance 10 :currency "USD" :user-id buyer} buyer-usd-wallet))
        (is (match? {:balance 107 :currency "USD" :user-id seller} seller-usd-wallet))
        (is (match? {:balance 3 :currency "BTC" :user-id seller} seller-btc-wallet))
        (is (match? {:balance 4 :currency "BTC" :user-id buyer} buyer-btc-wallet)))))

  (testing "if the orders don't match it shouldn't do anything"
    (let [fetch-wallet (spy/spy)
          match-orders (new-match-orders-use-case {:fetch-wallet fetch-wallet})
          bid (build-order {:price 40 :user-id 1})
          ask (build-order {:price 60 :user-id 2})]
      (is (nil? (match-orders {:bid bid :ask ask})))
      (assert/not-called? fetch-wallet))))
