(ns mercurius.wallets.domain.use-cases.deposit-test
  (:require [clojure.test :refer [deftest testing is]]
            [shrubbery.core :refer [mock received?]]
            [shrubbery.clojure.test]
            [mercurius.support.factory :refer [build-wallet]]
            [mercurius.wallets.domain.use-cases.deposit :refer [deposit-use-case]]
            [mercurius.core.domain.use-case :refer [execute]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository find-wallet save-wallet]]))

(deftest execute-test
  (testing "should load the wallet, make the deposit, and save the wallet"
    (let [wallet (build-wallet {:balance 100})
          repo (mock WalletRepository {:find-wallet wallet})]
      (execute (deposit-use-case repo) {:user-id 1 :amount 30 :currency "BTC"})
      (is (received? repo find-wallet [1 "BTC"]))
      (is (received? repo save-wallet [(assoc wallet :balance 130)])))))
