(ns mercurius.trading.domain.use-cases.execute-trades-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [spy.core :as spy]
            [spy.assert :as assert]
            [mercurius.support.factory :refer [build-wallet build-order]]
            [mercurius.trading.domain.use-cases.execute-trades :refer [new-execute-trades-use-case]]))

(def buyer 1)
(def seller 2)

(defn- build-use-case-for-wallets [& wallets-args]
  (let [[buyer-usd-wallet seller-usd-wallet seller-btc-wallet buyer-btc-wallet] (map build-wallet wallets-args)
        fetch-wallet (spy/mock (fn [user-id currency]
                                 (case [user-id currency]
                                   [1 "USD"] buyer-usd-wallet
                                   [2 "BTC"] seller-btc-wallet)))
        load-wallet (spy/mock (fn [user-id currency]
                                (case [user-id currency]
                                  [1 "BTC"] buyer-btc-wallet
                                  [2 "USD"] seller-usd-wallet)))
        save-wallet (spy/mock identity)
        deps {:fetch-wallet fetch-wallet
              :load-wallet load-wallet
              :save-wallet save-wallet}
        execute-trades (new-execute-trades-use-case deps)]
    [execute-trades deps]))

(deftest ^:integration execute-trades-test
  (testing "for each trade makes the corresponding transfers between the wallets"
    (let [[execute-trades deps]
          (build-use-case-for-wallets
           {:user-id buyer :currency "USD" :balance 110 :reserved 100}
           {:user-id seller :currency "USD" :balance 7}
           {:user-id seller :currency "BTC" :balance 5  :reserved 3}
           {:user-id buyer :currency "BTC" :balance 2})
          bid (build-order {:price 50 :amount 2 :side :buy :ticker "BTCUSD" :user-id buyer})
          ask (build-order {:price 50 :amount 2 :side :sell :ticker "BTCUSD" :user-id seller})]
      (is (match? [{:price 50}] (execute-trades {:bids [bid] :asks [ask]})))
      (let [[buyer-usd-wallet seller-usd-wallet seller-btc-wallet buyer-btc-wallet]
            (-> deps :save-wallet spy/calls flatten)]
        (is (match? {:user-id buyer :currency "USD" :balance 10 :reserved 0} buyer-usd-wallet))
        (is (match? {:user-id seller :currency "USD" :balance 107} seller-usd-wallet))
        (is (match? {:user-id seller :currency "BTC" :balance 3 :reserved 1} seller-btc-wallet))
        (is (match? {:user-id buyer :currency "BTC" :balance 4} buyer-btc-wallet)))))

  (testing "if the orders don't match it shouldn't do anything"
    (let [fetch-wallet (spy/spy)
          save-wallet (spy/spy)
          execute-trades (new-execute-trades-use-case {:fetch-wallet fetch-wallet :save-wallet save-wallet})]
      (is (= [] (execute-trades {:bids [(build-order)] :asks []})))
      (assert/not-called? fetch-wallet)
      (assert/not-called? save-wallet))))
