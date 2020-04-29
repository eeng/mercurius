(ns mercurius.trading.domain.use-cases.process-trade-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [matcher-combinators.matchers :as m]
            [spy.core :as spy]
            [mercurius.support.factory :as factory]
            [mercurius.trading.domain.use-cases.process-trade :refer [new-process-trade-use-case]]))

(defn build-use-case [deps]
  (new-process-trade-use-case
   (merge {:update-ticker identity
           :get-ticker identity
           :add-trade identity
           :publish-events identity}
          deps)))

(defn build-trade
  "Adds the expected event metadata to the trade."
  [args]
  (factory/build-trade (merge {:id "1" :created-at "..."} args)))

(deftest process-trade-test
  (testing "adds the trade to the repo"
    (let [add-trade (spy/spy)
          process-trade (build-use-case {:add-trade add-trade})]
      (process-trade (build-trade {:id "6" :ticker "BTCUSD" :price 50M :bid {} :ask {}}))
      (is (match? [[{:id "6" :ticker "BTCUSD" :price 50M :bid m/absent :ask m/absent :direction :up}]]
                  (spy/calls add-trade)))))

  (testing "publishes a :trade-processed event containing the two user-ids involved in the transaction"
    (let [publish-events (spy/spy)
          process-trade (build-use-case {:publish-events publish-events})]
      (process-trade (build-trade {:id "5" :ticker "ETHUSD" :bid {:user-id "7"} :ask {:user-id "8"}}))
      (is (match? [:trade-processed {:id "5" :ticker "ETHUSD" :users-involved ["7" "8"]}]
                  (-> (spy/calls publish-events) first first first)))))

  (testing "publishes a :ticker-updated event"
    (let [update-ticker (constantly {:ticker "BTCUSD" :last-price 100M :volume 900M})
          publish-events (spy/spy)
          process-trade (build-use-case {:update-ticker update-ticker
                                         :publish-events publish-events})]
      (process-trade (build-trade {}))
      (is (match? [:ticker-updated {:ticker "BTCUSD" :last-price 100M :volume 900M}]
                  (-> (spy/calls publish-events) first first last))))))
