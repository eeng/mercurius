(ns mercurius.wallets.domain.use-cases.transfer-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [spy.core :as spy]
            [spy.assert :as assert]
            [mercurius.support.factory :refer [build-wallet build-user-id]]
            [mercurius.wallets.domain.use-cases.transfer :refer [new-transfer-use-case]]))

(def u1 (build-user-id))
(def u2 (build-user-id))

(deftest transfer-test
  (testing "should get the two wallets, make the transfer, and save them"
    (let [w1 (build-wallet {:user-id u1 :currency "USD" :balance 100})
          w2 (build-wallet {:user-id u2 :currency "USD" :balance 50})
          fetch-wallet (spy/mock (constantly w1))
          load-wallet (spy/mock (constantly w2))
          save-wallet (spy/mock identity)
          transfer (new-transfer-use-case {:fetch-wallet fetch-wallet
                                           :load-wallet load-wallet
                                           :save-wallet save-wallet})]
      (transfer {:from u1 :to u2 :currency "USD" :transfer-amount 10})
      (assert/called-with? fetch-wallet u1 "USD")
      (assert/called-with? load-wallet u2 "USD")
      (is (match? [[{:user-id u1 :balance 90M}]
                   [{:user-id u2 :balance 60M}]]
                  (spy/calls save-wallet)))))

  (testing "should allow to cancel a reserved amount"
    (let [w1 (build-wallet {:user-id u1 :currency "USD" :balance 100 :reserved 30})
          w2 (build-wallet {:user-id u2 :currency "USD" :balance 0})
          save-wallet (spy/mock identity)
          transfer (new-transfer-use-case {:fetch-wallet (spy/stub w1)
                                           :load-wallet (spy/stub w2)
                                           :save-wallet save-wallet})]
      (transfer {:from u1 :to u2 :currency "USD" :transfer-amount 10 :cancel-amount 11})
      (is (match? [[{:user-id u1 :balance 90M :reserved 19M}]
                   [{:user-id u2 :balance 10M}]]
                  (spy/calls save-wallet))))))
