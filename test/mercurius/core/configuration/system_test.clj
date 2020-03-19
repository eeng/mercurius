(ns mercurius.core.configuration.system-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.core.configuration.system :refer [start stop]]
            [mercurius.core.controllers.mediator :refer [dispatch]]))

(defmacro with-system
  "Starts the system and makes sure it's stopped afterward.
  The bindings works like let where the second argument are passed to start (not implemented yet)."
  [bindings & body]
  `(let [system# (start)]
     (try
       (let [~(first bindings) system#]
         ~@body)
       (finally
         (stop system#)))))

(deftest ^:integration start-test
  (testing "Assembles the system and allows to execute the use cases"
    (with-system [{:keys [mediator] :as system} {}]
      (is (map? system))

      (dispatch mediator :deposit {:user-id 1 :amount 100 :currency "USD"})
      (dispatch mediator :withdraw {:user-id 1 :amount 30 :currency "USD"})
      (let [wallet (dispatch mediator :get-wallet {:user-id 1 :currency "USD"})]
        (is (match? {:balance 70} wallet)))

      (dispatch mediator :place-order {:user-id 1 :type :limit :side :buy
                                       :amount 0.2 :ticker "BTCUSD" :price 100})
      (let [{:keys [buying]} (dispatch mediator :get-order-book {:ticker "BTCUSD"})]
        (is (match? [{:amount 0.2}] buying))))))
