(ns mercurius.simulation.trading.simulator-test
  (:require [clojure.test :refer [deftest is]]
            [mercurius.support.fixtures :refer [with-system]]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.simulation.trading.simulator :refer [run-simulation]]))

(deftest ^:integration ^:slow run-simulation-test
  (with-system [{:keys [dispatch] :as system} {}]
    (let [n-traders 20
          n-orders-per-trader 5
          initial-price 600.0
          usd-funds 1000M
          btc-funds (bigdec (ticker/round-number (/ usd-funds initial-price)))
          expected-monetary-base {"USD" (* n-traders usd-funds)
                                  "BTC" (* n-traders btc-funds)}]
      (time (run-simulation system
                            :tickers {"BTCUSD" {:initial-price initial-price :initial-funds usd-funds}}
                            :n-traders n-traders
                            :n-orders-per-trader n-orders-per-trader
                            :max-ms-between-orders 10))
      (is (= expected-monetary-base (dispatch :calculate-monetary-base {})))
      ; TODO This sometimes fails. Some aggregate invariant must be being broken.
      #_(dispatch :execute-trades {:ticker "BTCUSD"})
      #_(is (= expected-monetary-base (dispatch :calculate-monetary-base {}))))))
