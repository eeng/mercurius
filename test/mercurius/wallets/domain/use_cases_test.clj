(ns mercurius.wallets.domain.use-cases-test
  (:require [clojure.test :refer [deftest testing is]]
            [shrubbery.core :refer [mock received?]]
            [shrubbery.clojure.test]
            [mercurius.wallets.domain.use-cases :refer [deposit withdraw]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository load-wallet save-wallet]]))

(deftest deposit-test
  (testing "should load the wallet, make the deposit, and save the wallet"
    (let [repo (mock WalletRepository {:load-wallet {:balance 100}
                                       :save-wallet true})]
      (deposit repo {:user-id 1 :amount 30 :currency "BTC"})
      (is (received? repo load-wallet [1 "BTC"]))
      (is (received? repo save-wallet [{:balance 130}])))))

(deftest withdraw-test
  (testing "should load the wallet, make the withdraw, and save the wallet"
    (let [repo (mock WalletRepository {:load-wallet {:balance 100}
                                       :save-wallet true})]
      (withdraw repo {:user-id 1 :amount 30 :currency "BTC"})
      (is (received? repo load-wallet [1 "BTC"]))
      (is (received? repo save-wallet [{:balance 70}])))))
