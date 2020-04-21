(ns mercurius.util.progress-test
  (:require [clojure.test :refer [deftest testing is]]
            [mercurius.util.progress :refer [new-progress-tracker]]))

(deftest new-progress-tracker-test
  (testing "returns a function that should allow to advance the progress"
    (let [{:keys [progress! current-progress]} (new-progress-tracker {:total 5})]
      (is (= 0.0 (current-progress)))
      (progress!)
      (is (= 0.2 (current-progress)))
      (progress!)
      (is (= 0.4 (current-progress)))))

  (testing "should clamp the progress to 1.0"
    (let [{:keys [progress! current-progress]} (new-progress-tracker {:total 5})]
      (dotimes [_ 7]
        (progress!))
      (is (= 1.0 (current-progress))))))
