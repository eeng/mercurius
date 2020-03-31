(ns mercurius.wallets.domain.use-cases.withdraw-test
  (:require [clojure.test :refer [deftest testing]]
            [spy.core :as spy]
            [spy.assert :as assert]
            [mercurius.support.factory :refer [build-user-id build-wallet]]
            [mercurius.wallets.domain.use-cases.withdraw :refer [new-withdraw-use-case]]))

(def bob (build-user-id))

(deftest withdraw-test
  (testing "should fetch the wallet, make the withdraw, and save the wallet"
    (let [wallet (build-wallet {:balance 100})
          fetch-wallet (spy/mock (constantly wallet))
          save-wallet (spy/mock identity)
          withdraw (new-withdraw-use-case {:fetch-wallet fetch-wallet :save-wallet save-wallet})]
      (withdraw {:user-id bob :amount 30 :currency "BTC"})
      (assert/called-with? fetch-wallet bob "BTC")
      (assert/called-with? save-wallet (assoc wallet :balance 70M)))))
