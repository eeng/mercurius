(ns mercurius.core.controllers.mediator-test
  (:require [clojure.test :refer [deftest testing is]]
            [shrubbery.core :refer [mock]]
            [shrubbery.clojure.test]
            [mercurius.core.domain.use-case :refer [UseCase]]
            [mercurius.core.controllers.mediator :refer [new-mediator dispatch]]))

(deftest dispatch-test
  (testing "should route the request to the appropiate handler"
    (let [uc1 (mock UseCase {:execute :result1})
          uc2 (mock UseCase {:execute :result2})
          mediator (new-mediator {:request-for-uc1 uc1 :request-for-uc2 uc2})]
      (is (= :result1 (dispatch mediator {:type :request-for-uc1})))
      (is (= :result2 (dispatch mediator {:type :request-for-uc2}))))))
