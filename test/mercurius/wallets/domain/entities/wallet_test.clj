(ns mercurius.wallets.domain.entities.wallet-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.support.factory :refer [build-wallet]]
            [mercurius.wallets.domain.entities.wallet :refer [deposit withdraw reserve transfer]]))

(deftest deposit-test
  (testing "should increase the wallet balance by the amount passed"
    (let [wallet (build-wallet)]
      (is (match? {:balance 150.2M}
                  (-> wallet (deposit 100) (deposit 50.2))))))

  (testing "amount should be positive"
    (is (thrown-match? clojure.lang.ExceptionInfo {:type :wallet/invalid-amount}
                       (deposit (build-wallet) -1)))
    (is (thrown-match? clojure.lang.ExceptionInfo {:type :wallet/invalid-amount}
                       (deposit (build-wallet) 0)))))

(deftest withdraw-test
  (testing "should decrease the wallet balance by the amount passed"
    (is (match? {:balance 69.5M}
                (-> (build-wallet {:balance 100}) (withdraw 10) (withdraw 20.5))))
    (is (match? {:balance 0M}
                (-> (build-wallet {:balance 30}) (withdraw 30)))))

  (testing "amount should be greater or equal to the available balance"
    (is (thrown-match? clojure.lang.ExceptionInfo {:type :wallet/insufficient-balance}
                       (-> (build-wallet {:balance 30}) (withdraw 31))))
    (is (thrown-match? clojure.lang.ExceptionInfo {:type :wallet/insufficient-balance}
                       (-> (build-wallet {:balance 30 :reserved 20}) (withdraw 11)))))

  (testing "amount should be positive"
    (let [wallet (build-wallet {:balance 5})]
      (is (thrown-match? clojure.lang.ExceptionInfo {:type :wallet/invalid-amount}
                         (withdraw wallet -1))))))

(deftest reserve-test
  (testing "should increase the wallet reserved amount by the amount passed"
    (is (match? {:reserved 30.7M} (-> (build-wallet {:balance 50}) (reserve 10) (reserve 20.7)))))

  (testing "amount should be positive"
    (is (thrown-match? clojure.lang.ExceptionInfo {:type :wallet/invalid-amount}
                       (reserve (build-wallet) -1))))

  (testing "amount must not be greater than the available balance"
    (is (thrown-match? clojure.lang.ExceptionInfo {:type :wallet/insufficient-balance}
                       (reserve (build-wallet) 1)))))

(deftest transfer-test
  (testing "should transfer the amount from the first wallet to the second one"
    (let [src (build-wallet {:balance 10})
          dst (build-wallet {:balance 5})]
      (is (match? [{:balance 7M} {:balance 8M}]
                  (transfer src dst 3)))))

  (testing "is not possible if the src wallet doesn't have enough balance"
    (let [src (build-wallet {:balance 0})
          dst (build-wallet {:balance 0})]
      (is (thrown-match? clojure.lang.ExceptionInfo {:type :wallet/insufficient-balance}
                         (transfer src dst 1)))))

  (testing "can't transfer between different currencies"
    (let [src (build-wallet {:balance 5 :currency "USD"})
          dst (build-wallet {:balance 0 :currency "BTC"})]
      (is (thrown-match? clojure.lang.ExceptionInfo
                         {:type :wallet/different-currencies}
                         (transfer src dst 1))))))
