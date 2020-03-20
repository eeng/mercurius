(ns mercurius.trading.adapters.repositories.in-memory-order-book-repository-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.trading.domain.entities.order :refer [new-order]]
            [mercurius.trading.domain.repositories.order-book-repository :refer [insert-order get-order-book get-bid-ask]]
            [mercurius.trading.adapters.repositories.in-memory-order-book-repository :refer [new-in-memory-order-book-repo]]))

(deftest insert-order-test
  (testing "should add the order to the corresponding side"
    (let [repo (new-in-memory-order-book-repo)]
      (insert-order repo (new-order {:ticker "BTCUSD" :side :buy}))
      (insert-order repo (new-order {:ticker "BTCUSD" :side :buy}))
      (insert-order repo (new-order {:ticker "BTCUSD" :side :sell}))
      (is (= 2 (-> (get-order-book repo "BTCUSD") :buying count)))
      (is (= 1 (-> (get-order-book repo "BTCUSD") :selling count)))))

  (testing "should keep a separate order book for each ticker"
    (let [repo (new-in-memory-order-book-repo)]
      (insert-order repo (new-order {:ticker "BTCUSD" :side :buy}))
      (insert-order repo (new-order {:ticker "ETHUSD" :side :buy}))
      (is (= 1 (-> (get-order-book repo "BTCUSD") :buying count)))
      (is (= 1 (-> (get-order-book repo "ETHUSD") :buying count))))))

(deftest get-order-book-test
  (testing "returns the buying side sorted by price desc"
    (let [repo (new-in-memory-order-book-repo)]
      (doseq [price [5 6 4]]
        (insert-order repo (new-order {:ticker "BTCUSD" :side :buy :price price})))
      (is (= [6 5 4] (->> (get-order-book repo "BTCUSD") :buying (map :price))))))

  (testing "returns the selling side sorted by price asc"
    (let [repo (new-in-memory-order-book-repo)]
      (doseq [price [5 6 4]]
        (insert-order repo (new-order {:ticker "BTCUSD" :side :sell :price price})))
      (is (= [4 5 6] (->> (get-order-book repo "BTCUSD") :selling (map :price)))))))

(deftest get-bid-ask-test
  (testing "returns a map with the bid and ask orders"
    (let [repo (new-in-memory-order-book-repo)
          o1 (insert-order repo (new-order {:ticker "BTCUSD" :side :buy :price 5}))
          _o2 (insert-order repo (new-order {:ticker "BTCUSD" :side :buy :price 4}))
          o3 (insert-order repo (new-order {:ticker "BTCUSD" :side :sell :price 6}))]
      (is (= {:bid o1 :ask o3}
             (get-bid-ask repo "BTCUSD")))))

  (testing "returns nil for the bid or ask if there is no order on the side"
    (let [repo (new-in-memory-order-book-repo)]
      (is (= {:bid nil :ask nil}
             (get-bid-ask repo "BTCUSD"))))))
