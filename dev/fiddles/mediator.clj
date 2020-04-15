(ns fiddles.mediator
  (:require [user :refer [system]]
            [mercurius.accounts.domain.repositories.user-repository :refer [find-by-username]]))

(comment
  (do
    (def dispatch (:use-cases/dispatch system))
    (def user-repo (:adapters/user-repo system))
    (def u1 (:id (find-by-username user-repo "user1")))
    (def u2 (:id (find-by-username user-repo "user1"))))

  (dispatch :deposit {:user-id u1 :amount 1000 :currency "USD"})
  (dispatch :deposit {:user-id u2 :amount 10 :currency "BTC"})

  (dispatch :place-order {:user-id u1 :side :buy :amount 3 :ticker "BTCUSD" :price 100 :type :limit})
  (dispatch :place-order {:user-id u2 :side :sell :amount 2 :ticker "BTCUSD" :price 90 :type :limit})
  (dispatch :place-order {:user-id u2 :side :sell :amount 4 :ticker "BTCUSD" :type :market})

  #_(dispatch :execute-trades {:ticker "BTCUSD"})
  (dispatch :get-tickers)
  (dispatch :get-trades {:ticker "BTCUSD"})

  (dispatch :get-order-book {:ticker "BTCUSD" :precision "P1" :limit 10})
  (dispatch :get-wallets {:user-id u1})
  (dispatch :get-wallets {:user-id u2})
  (dispatch :calculate-monetary-base)

  (dispatch :authenticate {:username "user1" :password "secret"}))
