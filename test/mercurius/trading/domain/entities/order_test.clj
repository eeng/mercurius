(ns mercurius.trading.domain.entities.order-test
  (:require [clojure.test :refer [deftest testing is]]
            [mercurius.trading.domain.entities.order :refer [reserve-currency]]))

(deftest reserve-currency-test
  (testing "on buy orders should reserve in the last currency"
    (is (= "USD" (reserve-currency :buy "BTCUSD")))
    (is (= "BTC" (reserve-currency :sell "BTCUSD")))
    (is (= "ETH" (reserve-currency :sell "ETHUSD")))))
