(ns mercurius.core.controllers.mediator-test
  (:require [clojure.test :refer [deftest testing is]]
            [shrubbery.clojure.test]
            [mercurius.core.controllers.mediator :refer [new-mediator dispatch]]))

(deftest dispatch-test
  (testing "should route the request to the appropiate handler"
    (let [uc1 (fn [request]
                (is (= request "data uc1"))
                :result1)
          uc2 (constantly :result2)
          mediator (new-mediator {:request-for-uc1 uc1 :request-for-uc2 uc2})]
      (is (= :result1 (dispatch mediator :request-for-uc1 "data uc1")))
      (is (= :result2 (dispatch mediator :request-for-uc2 "data uc2"))))))
