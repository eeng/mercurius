(ns fiddles.seed
  (:require [user :refer [system reset]]))

(comment
  (do
    (reset)
    (def dispatch (:use-cases/dispatch system))

    (dispatch :deposit {:user-id 1 :amount 10000 :currency "USD"})
    (dispatch :deposit {:user-id 2 :amount 100 :currency "BTC"})

    (dispatch :place-order {:user-id 1 :side :buy :amount 1.5 :ticker "BTCUSD" :price 1230.4 :type :limit})
    (dispatch :place-order {:user-id 1 :side :buy :amount 3 :ticker "BTCUSD" :price 1230.5 :type :limit})
    (dispatch :place-order {:user-id 2 :side :sell :amount 0.5 :ticker "BTCUSD" :price 1231.2 :type :limit})
    (dispatch :place-order {:user-id 2 :side :sell :amount 3 :ticker "BTCUSD" :price 1232.9 :type :limit})
    (dispatch :place-order {:user-id 2 :side :sell :amount 1 :ticker "BTCUSD" :price 1232.9 :type :limit}))

  ; Make a trade
  (let [dispatch (:use-cases/dispatch system)]
    (dispatch :place-order {:user-id 1 :side :buy :amount 0.1 :ticker "BTCUSD" :price 1234 :type :limit})))
