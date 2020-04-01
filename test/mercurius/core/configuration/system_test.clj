(ns mercurius.core.configuration.system-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.support.factory :refer [build-user-id]]
            [mercurius.support.helpers :refer [with-system]]))

(def u1 (build-user-id))
(def u2 (build-user-id))

(deftest ^:integration start-test
  (testing "assembles the system and allows to execute the use cases"
    (with-system [{:keys [dispatch] :as system} {}]
      (is (map? system))

      (dispatch :deposit {:user-id u1 :amount 100 :currency "USD"})
      (dispatch :withdraw {:user-id u1 :amount 30 :currency "USD"})
      (let [wallet (dispatch :get-wallet {:user-id u1 :currency "USD"})]
        (is (match? {:balance 70M} wallet)))

      (dispatch :place-order {:user-id u1 :type :limit :side :buy
                              :amount 0.2 :ticker "BTCUSD" :price 100})
      (let [{:keys [buying]} (dispatch :get-order-book {:ticker "BTCUSD"})]
        (is (match? [{:amount 0.2M}] buying)))))

  (testing "transfering between wallets should not have concurrency issues"
    (with-system [{:keys [dispatch]} {}]
      (let [concurrency 100
            amount (bigdec concurrency)]
        (dispatch :deposit {:user-id u1 :amount amount :currency "USD"})
        (->> #(dispatch :transfer {:from u1 :to u2 :transfer-amount 1 :currency "USD"})
             (repeat concurrency)
             (apply pcalls)
             (dorun))
        (is (match? {:balance 0M} (dispatch :get-wallet {:user-id u1 :currency "USD"})))
        (is (match? {:balance amount} (dispatch :get-wallet {:user-id u2 :currency "USD"})))))))
