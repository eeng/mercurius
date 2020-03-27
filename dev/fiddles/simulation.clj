(ns fiddles.simulation
  (:require [user :refer [system]]
            [mercurius.simulation.trading.simulator :refer [run-simulation]]))

(comment
  (time (run-simulation system
                        :tickers {"BTCUSD" {:initial-price 6000 :initial-funds 10000}}
                        :n-traders 100
                        :n-orders-per-trader 5
                        :max-ms-between-orders 10
                        :max-pos-size-pct 0.3
                        :spread-around-better-price [0.2 0.005]))

  (def dispatch (:dispatch system))
  (dispatch :calculate-monetary-base {})
  (dispatch :execute-trades {:ticker "BTCUSD"})
  (dispatch :get-order-book {:ticker "BTCUSD" :precision "P1" :limit 10}))
