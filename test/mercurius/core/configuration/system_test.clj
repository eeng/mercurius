(ns mercurius.core.configuration.system-test
  (:require [clojure.test :refer [deftest testing is]]
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

(deftest start-test
  (testing "Configures and start the entire system"
    (with-system [{:keys [mediator] :as system} {}]
      (is (map? system))
      (dispatch mediator :deposit {:user-id 1 :amount 100 :currency "USD"})
      (dispatch mediator :withdraw {:user-id 1 :amount 30 :currency "USD"})
      (dispatch mediator :place-order {:user-id 1 :type :limit :side :buy
                                       :amount 0.2 :ticker "BTCUSD" :price 100})
      (dispatch mediator :get-order-book {:ticker "BTCUSD"}))))
