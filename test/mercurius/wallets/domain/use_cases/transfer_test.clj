(ns mercurius.wallets.domain.use-cases.transfer-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [spy.core :as spy]
            [mercurius.support.factory :refer [build-wallet build-user-id]]
            [mercurius.wallets.domain.use-cases.transfer :refer [new-transfer-use-case]]))

(def u1 (build-user-id))
(def u2 (build-user-id))

(deftest transfer-test
  (testing "should get the two wallets, make the transfer, and save them"
    (let [w1 (build-wallet {:user-id u1 :currency "USD" :balance 100})
          w2 (build-wallet {:user-id u2 :currency "USD" :balance 50})
          load-wallet (spy/mock (fn [user-id _]
                                  (if (= user-id u1) w1 w2)))
          save-wallet (spy/mock identity)
          transfer (new-transfer-use-case {:load-wallet load-wallet
                                           :save-wallet save-wallet
                                           :publish-events identity})]
      (transfer {:from u1 :to u2 :currency "USD" :transfer-amount 10})
      (is (match? [[{:user-id u1 :balance 90M}]
                   [{:user-id u2 :balance 60M}]]
                  (spy/calls save-wallet)))))

  (testing "should allow to cancel a reserved amount"
    (let [w1 (build-wallet {:user-id u1 :currency "USD" :balance 100 :reserved 30})
          w2 (build-wallet {:user-id u2 :currency "USD" :balance 0})
          load-wallet (spy/mock (fn [user-id _]
                                  (if (= user-id u1) w1 w2)))
          save-wallet (spy/mock identity)
          transfer (new-transfer-use-case {:load-wallet load-wallet
                                           :save-wallet save-wallet
                                           :publish-events identity})]
      (transfer {:from u1 :to u2 :currency "USD" :transfer-amount 10 :cancel-amount 11})
      (is (match? [[{:user-id u1 :balance 90M :reserved 19M}]
                   [{:user-id u2 :balance 10M}]]
                  (spy/calls save-wallet))))))
