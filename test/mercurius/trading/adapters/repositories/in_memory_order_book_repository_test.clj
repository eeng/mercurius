(ns mercurius.trading.adapters.repositories.in-memory-order-book-repository-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [matcher-combinators.test]
            [mercurius.support.helpers :refer [ref-trx]]
            [mercurius.support.factory :refer [build-order]]
            [mercurius.trading.domain.repositories.order-book-repository :refer [insert-order get-order-book get-bids-asks update-order remove-order get-bid-ask]]
            [mercurius.trading.adapters.repositories.in-memory-order-book-repository :refer [new-in-memory-order-book-repo]]))

(use-fixtures :each ref-trx)

(deftest insert-order-test
  (testing "should add the order to the corresponding side"
    (let [repo (new-in-memory-order-book-repo)]
      (insert-order repo (build-order {:ticker "BTCUSD" :side :buy}))
      (insert-order repo (build-order {:ticker "BTCUSD" :side :buy}))
      (insert-order repo (build-order {:ticker "BTCUSD" :side :sell}))
      (is (= 2 (-> (get-order-book repo "BTCUSD") :buying count)))
      (is (= 1 (-> (get-order-book repo "BTCUSD") :selling count)))))

  (testing "should keep a separate order book for each ticker"
    (let [repo (new-in-memory-order-book-repo)]
      (insert-order repo (build-order {:ticker "BTCUSD" :side :buy}))
      (insert-order repo (build-order {:ticker "ETHUSD" :side :buy}))
      (is (= 1 (-> (get-order-book repo "BTCUSD") :buying count)))
      (is (= 1 (-> (get-order-book repo "ETHUSD") :buying count))))))

(deftest update-order-test
  (testing "updates the order by id"
    (let [repo (new-in-memory-order-book-repo)
          o1 (build-order {:id "O1" :ticker "BTCUSD" :side :buy :amount 5})
          o2 (build-order {:id "O2" :ticker "BTCUSD" :side :buy :amount 5})
          new-o1 (assoc o1 :remaining 3)]
      (doseq [order [o1 o2]] (insert-order repo order))
      (update-order repo new-o1)
      (is (match? [new-o1 o2] (:buying (get-order-book repo "BTCUSD")))))))

(deftest remove-order-test
  (testing "removes the order by id"
    (let [repo (new-in-memory-order-book-repo)
          o1 (build-order {:id "O1" :ticker "BTCUSD" :side :buy :amount 5})
          o2 (build-order {:id "O2" :ticker "BTCUSD" :side :buy :amount 5})
          new-o1 (assoc o1 :remaining 0)]
      (doseq [order [o1 o2]] (insert-order repo order))
      (remove-order repo new-o1)
      (is (match? [o2] (:buying (get-order-book repo "BTCUSD")))))))

(deftest get-order-book-test
  (testing "returns the buying side sorted by price desc"
    (let [repo (new-in-memory-order-book-repo)]
      (doseq [price [5 6 4]]
        (insert-order repo (build-order {:ticker "BTCUSD" :side :buy :price price})))
      (is (= [6 5 4] (->> (get-order-book repo "BTCUSD") :buying (map :price))))))

  (testing "returns the selling side sorted by price asc"
    (let [repo (new-in-memory-order-book-repo)]
      (doseq [price [5 6 4]]
        (insert-order repo (build-order {:ticker "BTCUSD" :side :sell :price price})))
      (is (= [4 5 6] (->> (get-order-book repo "BTCUSD") :selling (map :price)))))))

(deftest get-bids-asks-test
  (testing "returns a map with the bids and asks orders (those in the intersection of prices)"
    (let [repo (new-in-memory-order-book-repo)
          b1 (insert-order repo (build-order {:ticker "BTCUSD" :side :buy :price 5.1}))
          b2 (insert-order repo (build-order {:ticker "BTCUSD" :side :buy :price 5.0}))
          _b3 (insert-order repo (build-order {:ticker "BTCUSD" :side :buy :price 4.9}))
          s1 (insert-order repo (build-order {:ticker "BTCUSD" :side :sell :price 5.0}))
          _s2 (insert-order repo (build-order {:ticker "BTCUSD" :side :sell :price 5.2}))]
      (is (match? {:bids [b1 b2] :asks [s1]}
                  (get-bids-asks repo "BTCUSD")))))

  (testing "returns empty vector if there is no intersection of prices"
    (let [repo (new-in-memory-order-book-repo)]
      (insert-order repo (build-order {:ticker "BTCUSD" :side :buy :price 4.9}))
      (insert-order repo (build-order {:ticker "BTCUSD" :side :sell :price 5.1}))
      (is (= {:bids [] :asks []}
             (get-bids-asks repo "BTCUSD"))))))

(deftest get-bid-ask-test
  (testing "should return the best buying and selling order prices"
    (let [repo (new-in-memory-order-book-repo)]
      (insert-order repo (build-order {:ticker "BTCUSD" :side :buy :price 5.0}))
      (insert-order repo (build-order {:ticker "BTCUSD" :side :buy :price 5.2}))
      (insert-order repo (build-order {:ticker "BTCUSD" :side :buy :price 5.1}))
      (insert-order repo (build-order {:ticker "BTCUSD" :side :sell :price 5.9}))
      (is (match? {:bid 5.2 :ask 5.9}
                  (get-bid-ask repo "BTCUSD"))))))
