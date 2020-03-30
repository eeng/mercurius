(ns mercurius.trading.domain.use-cases.update-ticker-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [spy.core :as spy]
            [mercurius.support.factory :refer [build-trade]]
            [mercurius.trading.domain.use-cases.update-ticker :refer [new-update-ticker-use-case]]))

(deftest update-ticker-test
  (testing "publish a :ticker-changed event"
    (let [update-ticker (constantly {:ticker "BTCUSD" :last-price 100M :volume 900M})
          publish-event (spy/spy)
          use-case (new-update-ticker-use-case {:update-ticker update-ticker
                                                :publish-event publish-event})]
      (use-case (build-trade))
      (is (match? [[[:ticker-changed {:ticker "BTCUSD" :last-price 100M :volume 900M}]]]
                  (spy/calls publish-event))))))
