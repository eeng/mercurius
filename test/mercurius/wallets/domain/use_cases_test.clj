(ns mercurius.wallets.domain.use-cases-test
  (:require [clojure.test :refer [deftest testing is]]
            [shrubbery.core :refer [mock received?]]
            [shrubbery.clojure.test]
            [mercurius.support.factory :refer [build-wallet]]
            [mercurius.wallets.domain.use-cases :refer [deposit withdraw]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository load-wallet save-wallet]]))

(deftest deposit-test
  (testing "should load the wallet, make the deposit, and save the wallet"
    (let [wallet (build-wallet {:balance 100})
          repo (mock WalletRepository {:load-wallet wallet :save-wallet true})]
      (deposit repo {:user-id 1 :amount 30 :currency "BTC"})
      (is (received? repo load-wallet [1 "BTC"]))
      (is (received? repo save-wallet [(assoc wallet :balance 130)])))))

(deftest withdraw-test
  (testing "should load the wallet, make the withdraw, and save the wallet"
    (let [wallet (build-wallet {:balance 100})
          repo (mock WalletRepository {:load-wallet wallet :save-wallet true})]
      (withdraw repo {:user-id 1 :amount 30 :currency "BTC"})
      (is (received? repo load-wallet [1 "BTC"]))
      (is (received? repo save-wallet [(assoc wallet :balance 70)])))))
