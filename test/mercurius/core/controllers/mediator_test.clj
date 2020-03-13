(ns mercurius.core.controllers.mediator-test
  (:require [clojure.test :refer [deftest testing is]]
            [mercurius.core.controllers.mediator :refer [new-mediator dispatch]]))

(deftest dispatch-test
  (testing "should check specs"
    (let [mediator (new-mediator {:wallets/deposit (constantly true)})]
      (is (thrown? clojure.lang.ExceptionInfo
                   (dispatch mediator {:type :wallets/deposit :currency "Non existent"}))))))
