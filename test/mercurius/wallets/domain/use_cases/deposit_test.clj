(ns mercurius.wallets.domain.use-cases.deposit-test
  (:require [clojure.test :refer [deftest testing]]
            [spy.core :as spy]
            [spy.assert :as assert]
            [mercurius.support.factory :refer [build-user-id build-wallet]]
            [mercurius.wallets.domain.use-cases.deposit :refer [new-deposit-use-case]]))

(def bob (build-user-id))

(deftest deposit-test
  (testing "should load the wallet, make the deposit, and save the wallet"
    (let [wallet (build-wallet {:balance 100})
          load-wallet (spy/mock (constantly wallet))
          save-wallet (spy/mock identity)
          deposit (new-deposit-use-case {:load-wallet load-wallet :save-wallet save-wallet})]
      (deposit {:user-id bob :amount 30 :currency "BTC"})
      (assert/called-with? load-wallet bob "BTC")
      (assert/called-with? save-wallet (assoc wallet :balance 130M)))))
