(ns mercurius.wallets.domain.use-cases.withdraw-test
  (:require [clojure.test :refer [deftest testing is]]
            [shrubbery.core :refer [mock received?]]
            [shrubbery.clojure.test]
            [mercurius.support.factory :refer [build-wallet]]
            [mercurius.wallets.domain.use-cases.withdraw :refer [withdraw-use-case]]
            [mercurius.core.domain.use-case :refer [execute]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository load-wallet save-wallet]]))

(deftest execute-test
  (testing "should load the wallet, make the withdraw, and save the wallet"
    (let [wallet (build-wallet {:balance 100})
          repo (mock WalletRepository {:load-wallet wallet :save-wallet true})]
      (execute (withdraw-use-case repo) {:user-id 1 :amount 30 :currency "BTC"})
      (is (received? repo load-wallet [1 "BTC"]))
      (is (received? repo save-wallet [(assoc wallet :balance 70)])))))
