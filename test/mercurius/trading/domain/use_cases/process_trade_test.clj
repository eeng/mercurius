(ns mercurius.trading.domain.use-cases.process-trade-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [spy.core :as spy]
            [mercurius.support.factory :refer [build-trade]]
            [mercurius.trading.domain.use-cases.process-trade :refer [new-process-trade-use-case]]))

(deftest process-trade-test
  (testing "publish a :ticker-updated event"
    (let [update-ticker (constantly {:ticker "BTCUSD" :last-price 100M :volume 900M})
          publish-event (spy/spy)
          use-case (new-process-trade-use-case {:update-ticker update-ticker
                                                :add-trade identity
                                                :publish-event publish-event})]
      (use-case (assoc (build-trade) :id "1"))
      (is (match? [[[:ticker-updated {:ticker "BTCUSD" :last-price 100M :volume 900M}]]]
                  (spy/calls publish-event))))))
