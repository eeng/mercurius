(ns fiddles.simulation
  (:require [user :refer [system reset]]
            [mercurius.simulation.trading.simulator :refer [run-simulation]]))

(comment
  (do
    (reset)
    (def dispatch (:use-cases/dispatch system))
    (time (run-simulation dispatch
                          :tickers {"BTCUSD" {:initial-price 5000}}
                          :initial-funds {"USD" 10000 "BTC" 2}
                          :n-traders 100
                          :n-orders-per-trader 5
                          :max-ms-between-orders 100
                          :max-pos-size-pct 0.3
                          :spread-around-better-price [0.2 0.005]))
    (dispatch :calculate-monetary-base))

  (dispatch :get-order-book {:ticker "BTCUSD" :precision "P1" :limit 10})
  (dispatch :get-tickers))
