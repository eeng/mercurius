(ns mercurius.trading.domain.use-cases.match-orders-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [spy.core :as spy]
            [spy.assert :as assert]
            [mercurius.support.factory :refer [build-wallet]]
            [mercurius.trading.domain.use-cases.match-orders :refer [new-match-orders-use-case]]))

(deftest ^:kaocha/pending match-orders-test
  (testing "if the orders match should make two exchanges (one for each currency)")
  (testing "if the orders don't match it should do anything"))
