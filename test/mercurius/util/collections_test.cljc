(ns mercurius.util.collections-test
  (:require [clojure.test :refer [deftest testing is]]
            [mercurius.util.collections :refer [index-by]]))


(deftest index-by-test
  (testing "takes a collection of maps an returns a map where the keys are the value of the given field"
    (is (= {"a" {:id "a" :name "A"}
            "b" {:id "b" :name "B"}}
           (index-by :id [{:id "a" :name "A"}
                          {:id "b" :name "B"}])))))
