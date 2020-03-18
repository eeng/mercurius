(ns mercurius.wallets.domain.use-cases.withdraw-test
  (:require [clojure.test :refer [deftest testing is]]
            [shrubbery.core :refer [mock received?]]
            [shrubbery.clojure.test]
            [mercurius.support.asserts]
            [mercurius.support.factory :refer [build-wallet]]
            [mercurius.wallets.domain.use-cases.withdraw :refer [withdraw-use-case]]
            [mercurius.core.domain.use-case :refer [execute]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository find-wallet save-wallet]]))

(deftest execute-test
  (testing "should find the wallet, make the withdraw, and save the wallet"
    (let [wallet (build-wallet {:balance 100})
          repo (mock WalletRepository {:find-wallet wallet})]
      (execute (withdraw-use-case {:repo repo}) {:user-id 1 :amount 30 :currency "BTC"})
      (is (received? repo find-wallet [1 "BTC"]))
      (is (received? repo save-wallet [(assoc wallet :balance 70)]))))

  (testing "should not create the wallet if it doesn't exists"
    (let [repo (mock WalletRepository {:find-wallet nil})]
      (is (thrown-with-data? {:type :wallet/not-found}
                             (execute (withdraw-use-case {:repo repo}) {:user-id 1 :amount 30 :currency "BTC"}))))))
