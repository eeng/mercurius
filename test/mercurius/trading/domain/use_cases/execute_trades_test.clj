(ns mercurius.trading.domain.use-cases.execute-trades-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [spy.core :as spy]
            [spy.assert :as assert]
            [mercurius.support.factory :refer [build-order]]
            [mercurius.trading.domain.use-cases.execute-trades :refer [new-execute-trades-use-case]]))

(def buyer 1)
(def seller 2)

(defn- build-use-case
  [{:keys [get-bids-asks remove-order update-order transfer publish-event]
    :or {remove-order identity update-order identity publish-event identity transfer identity}}]
  (new-execute-trades-use-case {:get-bids-asks get-bids-asks
                                :update-order update-order
                                :remove-order remove-order
                                :transfer transfer
                                :publish-event publish-event}))

(deftest ^:integration execute-trades-test
  (testing "should publish an event for each trade made"
    (let [bid (build-order {:price 100 :amount 2.0 :side :buy :ticker "BTCUSD" :user-id buyer})
          ask1 (build-order {:price 100 :amount 0.5 :side :sell :ticker "BTCUSD" :user-id seller})
          ask2 (build-order {:price 100 :amount 1.5 :side :sell :ticker "BTCUSD" :user-id seller})
          get-bids-asks (spy/stub {:bids [bid] :asks [ask1 ask2]})
          publish-event (spy/mock identity)
          execute-trades (build-use-case {:get-bids-asks get-bids-asks :publish-event publish-event})]
      (execute-trades {:ticker "BTCUSD"})
      (is (match? [[[:trade-made {:price 100 :amount 0.5M}]]
                   [[:trade-made {:price 100 :amount 1.5M}]]]
                  (spy/calls publish-event)))))

  (testing "for each trade makes the corresponding transfers between the wallets"
    (let [bid (build-order {:price 50 :amount 2 :side :buy :ticker "BTCUSD" :user-id buyer})
          ask (build-order {:price 50 :amount 2 :side :sell :ticker "BTCUSD" :user-id seller})
          get-bids-asks (constantly {:bids [bid] :asks [ask]})
          transfer (spy/mock identity)
          execute-trades (build-use-case {:get-bids-asks get-bids-asks :transfer transfer})]
      (execute-trades {:ticker "BTCUSD"})
      (is (match? [[{:from buyer :to seller :currency "USD" :transfer-amount 100M :cancel-amount 100M}]
                   [{:from seller :to buyer :currency "BTC" :transfer-amount 2M :cancel-amount 2M}]]
                  (spy/calls transfer)))))

  (testing "should update the order book (updating or removing the order according to it's new state)"
    (let [bid (build-order {:price 50 :amount 1 :side :buy :ticker "BTCUSD" :user-id buyer})
          ask (build-order {:price 50 :amount 2 :side :sell :ticker "BTCUSD" :user-id seller})
          get-bids-asks (constantly {:bids [bid] :asks [ask]})
          update-order (spy/mock identity)
          remove-order (spy/mock identity)
          execute-trades (build-use-case {:get-bids-asks get-bids-asks
                                          :update-order update-order
                                          :remove-order remove-order})]
      (execute-trades {:ticker "BTCUSD"})
      (is (match? [[{:id (:id bid)}]] (spy/calls remove-order)))
      (is (match? [[{:id (:id ask)}]] (spy/calls update-order)))))

  (testing "if the orders don't match it shouldn't do anything"
    (let [transfer (spy/spy)
          publish-event (spy/spy)
          get-bids-asks (constantly {:bids [(build-order)] :asks []})
          execute-trades (new-execute-trades-use-case {:transfer transfer
                                                       :get-bids-asks get-bids-asks
                                                       :publish-event publish-event})]
      (execute-trades {:ticker "BTCUSD"})
      (assert/not-called? transfer)
      (assert/not-called? publish-event))))
