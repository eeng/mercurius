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
      (is (= 1.0 (current-progress)))))

  (testing "should allow to specify a callback to call when the progress has moved"
    (let [calls (atom [])
          {:keys [progress!]} (new-progress-tracker
                               {:total 100
                                :on-progress #(swap! calls conj %)})]
      (dotimes [_ 3]
        (progress!))
      (is (= [0.01 0.02 0.03] @calls))))

  (testing "should allow to jump straight to the end"
    (let [{:keys [finish! current-progress]} (new-progress-tracker {:total 5})]
      (finish!)
      (is (= 1.0 (current-progress))))))
