(ns mercurius.wallets.domain.use-cases.deposit-test
  (:require [clojure.test :refer [deftest testing]]
            [spy.core :as spy]
            [spy.assert :as assert]
            [mercurius.support.factory :refer [build-wallet]]
            [mercurius.wallets.domain.use-cases.deposit :refer [new-deposit-use-case]]))

(deftest execute-test
  (testing "should load the wallet, make the deposit, and save the wallet"
    (let [wallet (build-wallet {:balance 100})
          load-wallet (spy/mock (constantly wallet))
          save-wallet (spy/mock (constantly true))
          deposit (new-deposit-use-case {:load-wallet load-wallet :save-wallet save-wallet})]
      (deposit {:user-id 1 :amount 30 :currency "BTC"})
      (assert/called-with? load-wallet 1 "BTC")
      (assert/called-with? save-wallet (assoc wallet :balance 130)))))
