(ns mercurius.trading.adapters.repositories.in-memory-ticker-repository-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [mercurius.support.factory :refer [build-trade]]
            [mercurius.trading.domain.repositories.ticker-repository :refer [update-ticker get-ticker]]
            [mercurius.trading.adapters.repositories.in-memory-ticker-repository :refer [new-in-memory-ticker-repo]]))

(deftest update-ticker-test
  (testing "should set the new last-price"
    (let [repo (new-in-memory-ticker-repo)]
      (update-ticker repo (build-trade {:ticker "BTCUSD" :price 10.0}))
      (is (match? {:last-price 10.0} (get-ticker repo "BTCUSD")))
      (update-ticker repo (build-trade {:ticker "BTCUSD" :price 10.1}))
      (is (match? {:last-price 10.1} (get-ticker repo "BTCUSD")))

      (update-ticker repo (build-trade {:ticker "ETHUSD" :price 20.0}))
      (is (match? {:last-price 10.1} (get-ticker repo "BTCUSD")))
      (is (match? {:last-price 20.0} (get-ticker repo "ETHUSD")))))

  (testing "should increase the volume"
    (let [repo (new-in-memory-ticker-repo)]
      (is (match? {:volume 0M} (get-ticker repo "BTCUSD")))
      (update-ticker repo (build-trade {:ticker "BTCUSD" :amount 10M}))
      (is (match? {:volume 10M} (get-ticker repo "BTCUSD")))
      (update-ticker repo (build-trade {:ticker "BTCUSD" :amount 9M}))
      (is (match? {:volume 19M} (get-ticker repo "BTCUSD")))))

  (testing "should return the updated data"
    (let [repo (new-in-memory-ticker-repo)]
      (is (match? {:volume 5M :ticker "BTCUSD"}
                  (update-ticker repo (build-trade {:ticker "BTCUSD" :amount 5M})))))))
