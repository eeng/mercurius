(ns mercurius.core.domain.use-cases.mediator-test
  (:require [clojure.test :refer [deftest testing is]]
            [mercurius.core.domain.use-cases.mediator :refer [new-mediator dispatch]]))

(deftest dispatch-test
  (testing "should route the request to the appropiate handler"
    (let [uc1 (fn [data] [:result1 data])
          uc2 (fn [data] [:result2 data])
          mediator (new-mediator {:request-for-uc1 uc1 :request-for-uc2 uc2})]
      (is (= [:result1 "data1"] (dispatch mediator :request-for-uc1 "data1")))
      (is (= [:result2 "data2"] (dispatch mediator :request-for-uc2 "data2")))))

  (testing "allows to include middleware functions"
    (let [uc (constantly :ok)
          processed (atom [])
          m1 (fn [next]
               (fn [{:keys [data] :as req}]
                 (swap! processed conj ["in m1" data])
                 (next req)))
          m2 (fn [next]
               (fn [{:keys [data] :as req}]
                 (swap! processed conj ["in m2" data])
                 (next req)))
          mediator (new-mediator {:some-req uc}
                                 [m1 m2])]
      (is (= :ok (dispatch mediator :some-req "some data")))
      (is (= [["in m1" "some data"] ["in m2" "some data"]] @processed)))))
