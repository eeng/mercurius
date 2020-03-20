(ns mercurius.trading.domain.entities.order-test
  (:require [clojure.test :refer [deftest testing is]]
            [mercurius.trading.domain.entities.order :refer [currency-delivered]]))

(deftest currency-delivered-test
  (testing "returns the last currency when buying and the first one when selling"
    (is (= "USD" (currency-delivered :buy "BTCUSD")))
    (is (= "BTC" (currency-delivered :sell "BTCUSD")))
    (is (= "ETH" (currency-delivered :sell "ETHUSD")))))
