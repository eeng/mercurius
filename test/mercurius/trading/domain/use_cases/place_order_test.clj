(ns mercurius.trading.domain.use-cases.place-order-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [spy.core :as spy]
            [spy.assert :as assert]
            [mercurius.support.factory :refer [build-user-id build-wallet]]
            [mercurius.trading.domain.use-cases.place-order :refer [new-place-order-use-case]]))

(def bob (build-user-id))

(defn- build-use-case [deps]
  (new-place-order-use-case
   (merge {:save-wallet identity
           :insert-order identity
           :publish-event identity}
          deps)))

(deftest place-order-test
  (testing "should make a reservation in the user's wallet"
    (let [wallet (build-wallet {:balance 50 :currency "USD"})
          load-wallet (spy/mock (constantly wallet))
          save-wallet (spy/mock identity)
          place-order (build-use-case {:load-wallet load-wallet
                                       :save-wallet save-wallet})]
      (place-order {:user-id bob :type :limit :side :buy :amount 0.2 :ticker "BTCUSD" :price 100})
      (assert/called-with? load-wallet bob "USD")
      (assert/called-with? save-wallet (assoc wallet :reserved (* 0.2M 100)))))

  (testing "should insert the order in the order book"
    (let [wallet (build-wallet {:balance 50 :currency "USD"})
          insert-order (spy/mock identity)
          place-order (build-use-case {:load-wallet (constantly wallet)
                                       :insert-order insert-order})]
      (place-order {:user-id bob :type :limit :side :buy :amount 0.1 :ticker "BTCUSD" :price 100})
      (let [[[order]] (spy/calls insert-order)]
        (is (match? {:side :buy :amount 0.1M :price 100 :id some? :placed-at some?} order))))))
