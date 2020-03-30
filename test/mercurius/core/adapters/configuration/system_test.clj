(ns mercurius.core.adapters.configuration.system-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.support.fixtures :refer [with-system]]))

(deftest ^:integration start-test
  (testing "assembles the system and allows to execute the use cases"
    (with-system [{:keys [dispatch] :as system} {}]
      (is (map? system))

      (dispatch :deposit {:user-id 1 :amount 100 :currency "USD"})
      (dispatch :withdraw {:user-id 1 :amount 30 :currency "USD"})
      (let [wallet (dispatch :get-wallet {:user-id 1 :currency "USD"})]
        (is (match? {:balance 70M} wallet)))

      (dispatch :place-order {:user-id 1 :type :limit :side :buy
                              :amount 0.2 :ticker "BTCUSD" :price 100})
      (let [{:keys [buying]} (dispatch :get-order-book {:ticker "BTCUSD"})]
        (is (match? [{:amount 0.2M}] buying))))))
