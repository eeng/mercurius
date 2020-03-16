(ns mercurius.trading.domain.use-cases.place-order-test
  (:require [clojure.test :refer [deftest testing is]]
            [shrubbery.core :refer [mock received?]]
            [shrubbery.clojure.test]
            [mercurius.support.asserts :refer [submap?]]
            [mercurius.support.factory :refer [build-wallet]]
            [mercurius.trading.domain.use-cases.place-order :refer [place-order-use-case]]
            [mercurius.core.domain.use-case :refer [execute]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [WalletRepository find-wallet save-wallet]]
            [mercurius.trading.domain.repositories.order-book-repository :refer [OrderBookRepository insert-order]]))

(deftest execute-test
  (testing "for a buy order, should reserve the corresponding amount in the last currency's wallet"
    (let [wallet (build-wallet {:balance 50 :currency "USD"})
          wallet-repo (mock WalletRepository {:find-wallet wallet})
          place-order (place-order-use-case {:wallet-repo wallet-repo
                                             :order-book-repo (mock OrderBookRepository)})]
      (execute place-order {:user-id 1 :type :limit :side :buy :amount 0.2 :ticker "BTCUSD" :price 100})
      (is (received? wallet-repo find-wallet [1 "USD"]))
      (is (received? wallet-repo save-wallet [(assoc wallet :reserved (* 0.2 100))]))))

  (testing "for a sell order, should reserve the amount in the first currency's wallet"
    (let [wallet (build-wallet {:balance 1 :currency "BTC"})
          wallet-repo (mock WalletRepository {:find-wallet wallet})
          place-order (place-order-use-case {:wallet-repo wallet-repo
                                             :order-book-repo (mock OrderBookRepository)})]
      (execute place-order {:user-id 1 :type :limit :side :sell :amount 0.2 :ticker "BTCUSD" :price 100})
      (is (received? wallet-repo find-wallet [1 "BTC"]))
      (is (received? wallet-repo save-wallet [(assoc wallet :reserved 0.2)]))))

  (testing "should insert the order in the order book"
    (let [wallet (build-wallet {:balance 50 :currency "USD"})
          order-book-repo (mock OrderBookRepository)
          place-order (place-order-use-case {:wallet-repo (mock WalletRepository {:find-wallet wallet})
                                             :order-book-repo order-book-repo})]
      (execute place-order {:user-id 1 :type :limit :side :buy :amount 0.1 :ticker "BTCUSD" :price 100})
      (is (received? order-book-repo insert-order [#(submap? {:side :buy :amount 0.1 :price 100} %)])))))
