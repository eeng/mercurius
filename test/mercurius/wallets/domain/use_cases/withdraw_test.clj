(ns mercurius.wallets.domain.use-cases.withdraw-test
  (:require [clojure.test :refer [deftest testing is]]
            [spy.core :as spy]
            [spy.assert :as assert]
            [matcher-combinators.test]
            [mercurius.support.factory :refer [build-user-id build-wallet]]
            [mercurius.wallets.domain.use-cases.withdraw :refer [new-withdraw-use-case]]))

(def bob (build-user-id))

(deftest withdraw-test
  (testing "should fetch the wallet, make the withdraw, and save the wallet"
    (let [wallet (build-wallet {:balance 100})
          load-wallet (spy/mock (constantly wallet))
          save-wallet (spy/mock identity)
          withdraw (new-withdraw-use-case {:load-wallet load-wallet
                                           :save-wallet save-wallet
                                           :publish-events identity})]
      (withdraw {:user-id bob :amount 30 :currency "BTC"})
      (assert/called-with? load-wallet bob "BTC")
      (is (match? [[{:id (:id wallet) :balance 70M}]]
                  (spy/calls save-wallet))))))
