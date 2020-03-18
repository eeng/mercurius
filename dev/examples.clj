(ns examples
  (:require [user :refer [system]]
            [mercurius.core.controllers.mediator :refer [dispatch]]))

(comment
  (def mediator (:mediator system))

  (dispatch mediator :deposit {:user-id 1 :amount 100 :currency "USD"})
  (dispatch mediator :withdraw {:user-id 1 :amount 30 :currency "USD"})
  (dispatch mediator :deposit {:user-id 1 :amount 200 :currency "BTC"})
  (dispatch mediator :deposit {:user-id 2 :amount 50 :currency "USD"})
  (dispatch mediator :deposit {:user-id 2 :amount 10 :currency "ETH"})

  (dispatch mediator :place-order {:user-id 1 :type :limit :side :buy
                                   :amount 0.2 :ticker "BTCUSD" :price 100})
  (dispatch mediator :place-order {:user-id 1 :type :limit :side :sell
                                   :amount 0.5 :ticker "BTCUSD" :price 100})
  (dispatch mediator :place-order {:user-id 2 :type :limit :side :buy
                                   :amount 0.3 :ticker "ETHUSD" :price 110})

  (dispatch mediator :get-order-book {:ticker "BTCUSD"}))
