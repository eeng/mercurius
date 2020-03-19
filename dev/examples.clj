(ns examples
  (:require [user :refer [system]]
            [mercurius.core.controllers.mediator :as m]))

(comment
  (def dispatch (partial m/dispatch (:mediator system)))

  (dispatch :deposit {:user-id 1 :amount 100 :currency "USD"})
  (dispatch :withdraw {:user-id 1 :amount 30 :currency "USD"})
  (dispatch :deposit {:user-id 1 :amount 200 :currency "BTC"})
  (dispatch :deposit {:user-id 2 :amount 50 :currency "USD"})
  (dispatch :deposit {:user-id 2 :amount 10 :currency "ETH"})

  (dispatch :place-order {:user-id 1 :side :buy :amount 0.2 :ticker "BTCUSD" :price 100 :type :limit})
  (dispatch :place-order {:user-id 1 :side :sell :amount 0.5 :ticker "BTCUSD" :price 100 :type :limit})
  (dispatch :place-order {:user-id 2 :side :buy :amount 0.3 :ticker "ETHUSD" :price 110 :type :limit})

  (dispatch :get-order-book {:ticker "BTCUSD"}))
