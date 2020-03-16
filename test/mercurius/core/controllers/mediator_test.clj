(ns mercurius.core.controllers.mediator-test
  (:require [clojure.test :refer [deftest testing is]]
            [shrubbery.core :refer [mock received?]]
            [shrubbery.clojure.test]
            [mercurius.core.domain.use-case :refer [UseCase execute]]
            [mercurius.core.controllers.mediator :refer [new-mediator dispatch]]))

(deftest dispatch-test
  (testing "should route the request to the appropiate handler"
    (let [uc1 (mock UseCase {:execute :result1})
          uc2 (mock UseCase {:execute :result2})
          mediator (new-mediator {:request-for-uc1 uc1 :request-for-uc2 uc2})]
      (is (= :result1 (dispatch mediator :request-for-uc1 "data uc1")))
      (is (= :result2 (dispatch mediator :request-for-uc2 "data uc2")))
      (is (received? uc1 execute ["data uc1"]))
      (is (received? uc2 execute ["data uc2"])))))
