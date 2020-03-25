(ns mercurius.util.number-test
  (:require [clojure.test :refer [deftest testing is]]
            [mercurius.util.number :refer [round-to-decimal-places round-to-significant-figures]]))

(deftest round-to-decimal-places-test
  (testing "should round the number to the specified power of ten exponent"
    (is (= 5316.19 (round-to-decimal-places 5316.1943372 2)))
    (is (= 5316.2 (round-to-decimal-places 5316.1943372 1)))
    (is (= 5316.0 (round-to-decimal-places 5316.1943372 0)))
    (is (= 5320.0 (round-to-decimal-places 5316.1943372 -1)))
    (is (= 5300.0 (round-to-decimal-places 5316.1943372 -2)))))

(deftest round-to-significant-figures-test
  (testing "should leave only the specified number of digits"
    (is (= 5316.2 (round-to-significant-figures 5316.1913372 5)))
    (is (= 5316.19 (round-to-significant-figures 5316.1913372 6)))
    (is (= 531.62 (round-to-significant-figures 531.61913372 5)))
    (is (= 531.619 (round-to-significant-figures 531.61913372 6)))
    (is (= 0.012345 (round-to-significant-figures 0.0123451111 5)))
    (is (= 0.0123456 (round-to-significant-figures 0.0123456111 6)))
    (is (= 0.0012345 (round-to-significant-figures 0.00123451111 5)))
    (is (= 0.00123456 (round-to-significant-figures 0.00123456111 6)))
    (is (= 100.0 (round-to-significant-figures 100 3)))
    (is (= 100.0 (round-to-significant-figures 100 2)))))
