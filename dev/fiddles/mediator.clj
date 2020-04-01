(ns fiddles.mediator
  (:require [user :refer [system]]))

(comment
  (def dispatch (:controllers/dispatch system))

  (dispatch :deposit {:user-id 1 :amount 1000 :currency "USD"})
  (dispatch :deposit {:user-id 2 :amount 10 :currency "BTC"})

  (dispatch :place-order {:user-id 1 :side :buy :amount 3 :ticker "BTCUSD" :price 100 :type :limit})
  (dispatch :place-order {:user-id 2 :side :sell :amount 2 :ticker "BTCUSD" :price 90 :type :limit})
  (dispatch :place-order {:user-id 2 :side :sell :amount 4 :ticker "BTCUSD" :type :market})

  #_(dispatch :execute-trades {:ticker "BTCUSD"})
  (dispatch :get-tickers)

  (dispatch :get-order-book {:ticker "BTCUSD" :precision "P1" :limit 10})
  (dispatch :get-wallets {:user-id 1})
  (dispatch :get-wallets {:user-id 2})
  (dispatch :calculate-monetary-base))
