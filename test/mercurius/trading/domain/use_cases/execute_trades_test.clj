(ns mercurius.trading.domain.use-cases.execute-trades-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [spy.core :as spy]
            [spy.assert :as assert]
            [mercurius.support.factory :refer [build-wallet build-order]]
            [mercurius.trading.domain.use-cases.execute-trades :refer [new-execute-trades-use-case]]))

(def buyer 1)
(def seller 2)

(defn- build-use-case-for-wallets
  [wallets-args {:keys [get-bids-asks save-wallet remove-order update-order publish-event]
                 :or {save-wallet identity remove-order identity update-order identity publish-event identity}}]
  (let [[buyer-usd-wallet seller-usd-wallet seller-btc-wallet buyer-btc-wallet] (map build-wallet wallets-args)
        fetch-wallet (spy/mock (fn [user-id currency]
                                 (case [user-id currency]
                                   [1 "USD"] buyer-usd-wallet
                                   [2 "BTC"] seller-btc-wallet)))
        load-wallet (spy/mock (fn [user-id currency]
                                (case [user-id currency]
                                  [1 "BTC"] buyer-btc-wallet
                                  [2 "USD"] seller-usd-wallet)))
        execute-trades (new-execute-trades-use-case {:fetch-wallet fetch-wallet
                                                     :load-wallet load-wallet
                                                     :save-wallet save-wallet
                                                     :get-bids-asks get-bids-asks
                                                     :update-order update-order
                                                     :remove-order remove-order
                                                     :publish-event publish-event})]
    execute-trades))

(deftest ^:integration execute-trades-test
  (testing "should publish an event for each trade made"
    (let [bid (build-order {:price 100 :amount 1 :side :buy :ticker "BTCUSD" :user-id buyer})
          ask (build-order {:price 100 :amount 1 :side :sell :ticker "BTCUSD" :user-id seller})
          get-bids-asks (constantly {:bids [bid] :asks [ask]})
          publish-event (spy/mock identity)
          execute-trades (build-use-case-for-wallets
                          [{:user-id buyer :currency "USD" :balance 100 :reserved 100}
                           {:user-id seller :currency "USD"}
                           {:user-id seller :currency "BTC" :balance 1 :reserved 1}
                           {:user-id buyer :currency "BTC"}]
                          {:get-bids-asks get-bids-asks :save-wallet identity :publish-event publish-event})]
      (execute-trades {:ticker "BTCUSD"})
      (is (match? [[[:trading/trade-made {:price 100}]]] (spy/calls publish-event)))))

  (testing "for each trade makes the corresponding transfers between the wallets"
    (let [bid (build-order {:price 50 :amount 2 :side :buy :ticker "BTCUSD" :user-id buyer})
          ask (build-order {:price 50 :amount 2 :side :sell :ticker "BTCUSD" :user-id seller})
          get-bids-asks (constantly {:bids [bid] :asks [ask]})
          save-wallet (spy/mock identity)
          execute-trades (build-use-case-for-wallets
                          [{:user-id buyer :currency "USD" :balance 110 :reserved 100}
                           {:user-id seller :currency "USD" :balance 7}
                           {:user-id seller :currency "BTC" :balance 5 :reserved 3}
                           {:user-id buyer :currency "BTC" :balance 2}]
                          {:get-bids-asks get-bids-asks :save-wallet save-wallet})]
      (execute-trades {:ticker "BTCUSD"})
      (let [[buyer-usd-wallet seller-usd-wallet seller-btc-wallet buyer-btc-wallet]
            (-> save-wallet spy/calls flatten)]
        (is (match? {:user-id buyer :currency "USD" :balance 10M :reserved 0M} buyer-usd-wallet))
        (is (match? {:user-id seller :currency "USD" :balance 107M} seller-usd-wallet))
        (is (match? {:user-id seller :currency "BTC" :balance 3M :reserved 1M} seller-btc-wallet))
        (is (match? {:user-id buyer :currency "BTC" :balance 4M} buyer-btc-wallet)))))

  (testing "should update the order book (updating or removing the order according to it's new state)"
    (let [bid (build-order {:price 50 :amount 1 :side :buy :ticker "BTCUSD" :user-id buyer})
          ask (build-order {:price 50 :amount 2 :side :sell :ticker "BTCUSD" :user-id seller})
          get-bids-asks (constantly {:bids [bid] :asks [ask]})
          update-order (spy/mock identity)
          remove-order (spy/mock identity)
          execute-trades (build-use-case-for-wallets
                          [{:user-id buyer :currency "USD" :balance 50M :reserved 50M}
                           {:user-id seller :currency "USD" :balance 0M}
                           {:user-id seller :currency "BTC" :balance 2M :reserved 2M}
                           {:user-id buyer :currency "BTC" :balance 0M}]
                          {:get-bids-asks get-bids-asks :update-order update-order :remove-order remove-order})]
      (execute-trades {:ticker "BTCUSD"})
      (is (match? [[{:id (:id bid)}]] (spy/calls remove-order)))
      (is (match? [[{:id (:id ask)}]] (spy/calls update-order)))))

  (testing "if the orders don't match it shouldn't do anything"
    (let [fetch-wallet (spy/spy)
          save-wallet (spy/spy)
          publish-event (spy/spy)
          get-bids-asks (constantly {:bids [(build-order)] :asks []})
          execute-trades (new-execute-trades-use-case {:fetch-wallet fetch-wallet
                                                       :save-wallet save-wallet
                                                       :get-bids-asks get-bids-asks
                                                       :publish-event publish-event})]
      (execute-trades {:ticker "BTCUSD"})
      (assert/not-called? fetch-wallet)
      (assert/not-called? save-wallet)
      (assert/not-called? publish-event))))
