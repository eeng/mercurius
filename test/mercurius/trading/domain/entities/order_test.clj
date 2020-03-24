(ns mercurius.trading.domain.entities.order-test
  (:require [clojure.test :refer [deftest testing is]]
            [mercurius.trading.domain.entities.order :refer [currency-paid]]))

(deftest currency-paid-test
  (testing "returns the last currency when buying and the first one when selling"
    (is (= "USD" (currency-paid :buy "BTCUSD")))
    (is (= "BTC" (currency-paid :sell "BTCUSD")))
    (is (= "ETH" (currency-paid :sell "ETHUSD")))))
