(ns mercurius.wallets.domain.entities.wallet-test
  (:require [clojure.test :refer [deftest testing is]]
            [mercurius.support.asserts]
            [mercurius.support.factory :refer [build-wallet]]
            [mercurius.wallets.domain.entities.wallet :refer [deposit withdraw reserve]]))

(deftest deposit-test
  (testing "should increase the wallet balance by the amount passed"
    (let [wallet (build-wallet)
          new-wallet (-> wallet (deposit 100) (deposit 50))]
      (is (= 150 (:balance new-wallet)))))

  (testing "amount should be positive"
    (is (thrown-with-data? {:type :wallet/invalid-amount}
                           (deposit {:balance 0} -1)))
    (is (thrown-with-data? {:type :wallet/invalid-amount}
                           (deposit {:balance 0} 0)))))

(deftest withdraw-test
  (testing "should decrease the wallet balance by the amount passed"
    (is (= 70 (-> (build-wallet {:balance 100}) (withdraw 10) (withdraw 20) :balance)))
    (is (= 0 (-> (build-wallet {:balance 30}) (withdraw 30) :balance))))

  (testing "amount should be greater or equal to the available balance"
    (is (thrown-with-data? {:type :wallet/not-enough-balance}
                           (-> (build-wallet {:balance 30}) (withdraw 31))))
    (is (thrown-with-data? {:type :wallet/not-enough-balance}
                           (-> (build-wallet {:balance 30 :reserved 20}) (withdraw 11)))))

  (testing "amount should be positive"
    (let [wallet (build-wallet {:balance 5})]
      (is (thrown-with-data? {:type :wallet/invalid-amount}
                             (withdraw wallet -1))))))

(deftest reserve-test
  (testing "should increase the wallet reserved amount by the amount passed"
    (is (= 30 (-> (build-wallet {:balance 30}) (reserve 10) (reserve 20) :reserved))))

  (testing "amount should be positive"
    (is (thrown-with-data? {:type :wallet/invalid-amount}
                           (reserve (build-wallet) -1))))

  (testing "amount must not be greater than the available balance"
    (is (thrown-with-data? {:type :wallet/not-enough-balance}
                           (reserve (build-wallet) 1)))))
