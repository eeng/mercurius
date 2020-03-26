(ns examples
  (:require [user :refer [system]]
            [mercurius.simulation.trading.simulator :refer [run-simulation]]))

(comment
  (def dispatch (:dispatch system))

  (dispatch :deposit {:user-id 1 :amount 1000 :currency "USD"})
  (dispatch :deposit {:user-id 2 :amount 10 :currency "BTC"})

  (dispatch :place-order {:user-id 1 :side :buy :amount 3 :ticker "BTCUSD" :price 100 :type :limit})
  (dispatch :place-order {:user-id 2 :side :sell :amount 2 :ticker "BTCUSD" :price 90 :type :limit})
  (dispatch :place-order {:user-id 2 :side :sell :amount 4 :ticker "BTCUSD" :price 100 :type :limit})

  (dispatch :execute-trades {:ticker "BTCUSD"})

  (dispatch :calculate-monetary-base {})
  (dispatch :get-order-book {:ticker "BTCUSD" :precision "P1" :limit 20})
  (dispatch :get-wallets {:user-id 1})
  (dispatch :get-wallets {:user-id 2})

  (time (run-simulation system
                        :tickers {"BTCUSD" {:initial-price 6000 :initial-funds 10000}}
                        :n-traders 1000
                        :n-orders-per-trader 5
                        :max-ms-between-orders 10
                        :max-pos-size-pct 0.3
                        :spread-around-better-price [0.2 0.005]))

  (let [orders (-> (:order-book-repo system) :db deref (get "BTCUSD") vals flatten)]
    (->> orders
         (filter #(and (= (:user-id %) 128) (= (:side %) :buy)))
         (map #(* (:price %) (:amount %))))))
