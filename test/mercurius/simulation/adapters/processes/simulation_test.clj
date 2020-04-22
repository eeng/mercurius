(ns mercurius.simulation.adapters.processes.simulation-test
  (:require [clojure.test :refer [deftest is testing]]
            [mercurius.support.helpers :refer [with-system]]
            [mercurius.simulation.adapters.processes.simulation :refer [run-simulation]]
            [mercurius.util.progress :refer [new-progress-tracker]]))

(deftest ^:integration ^:slow run-simulation-test
  (testing "should not have concurrency issues"
    (with-system [{:use-cases/keys [dispatch]} {}]
      (let [n-traders 20
            n-orders-per-trader 5
            initial-price 250.0
            usd-funds 1000M
            btc-funds (bigdec (/ usd-funds initial-price))
            expected-monetary-base {"USD" (* n-traders usd-funds)
                                    "BTC" (* n-traders btc-funds)}]
        (run-simulation {:tickers {"BTCUSD" {:initial-price initial-price}}
                         :initial-funds {"USD" usd-funds "BTC" btc-funds}
                         :n-traders n-traders
                         :n-orders-per-trader n-orders-per-trader
                         :max-ms-between-orders 10}
                        {:dispatch dispatch
                         :running (atom true)
                         :progress (new-progress-tracker {:total 1})})
        (is (= expected-monetary-base (dispatch :calculate-monetary-base)))))))
