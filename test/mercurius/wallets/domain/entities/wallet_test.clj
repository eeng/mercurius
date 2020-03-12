(ns mercurius.wallets.domain.entities.wallet-test
  (:require [clojure.test :refer [deftest testing is]]
            [support.asserts]
            [mercurius.wallets.domain.entities.wallet :as w :refer [deposit withdraw]]))

(deftest deposit-test
  (testing "should increase the wallet balance by the amount passed"
    (let [wallet {:balance 0}
          new-wallet (-> wallet (deposit 100) (deposit 50))]
      (is (= 150 (new-wallet :balance)))))

  (testing "amount should be positive"
    (is (thrown-with-data? {:type ::w/invalid-deposit-amount}
                           (deposit {:balance 0} -1)))
    (is (thrown-with-data? {:type ::w/invalid-deposit-amount}
                           (deposit {:balance 0} 0)))))

(deftest withdraw-test
  (testing "should decrease the wallet balance by the amount passed"
    (let [wallet {:balance 100}
          new-wallet (-> wallet (withdraw 10) (withdraw 20))]
      (is (= 70 (new-wallet :balance)))))

  (testing "should not be possible to overdraw the wallet"
    (let [wallet {:balance 30}]
      (is (thrown-with-data? {:type ::w/wallet-overdrawn}
                             (withdraw wallet 31)))))

  (testing "amount should be positive"
    (is (thrown-with-data? {:type ::w/invalid-withdraw-amount}
                           (withdraw {:balance 5} -1)))))
