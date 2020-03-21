(ns examples
  (:require [user :refer [system]]
            [mercurius.core.adapters.controllers.mediator :as m]))

(comment
  (def dispatch (partial m/dispatch (:mediator system)))

  (dispatch :deposit {:user-id 1 :amount 100 :currency "USD"})
  (dispatch :withdraw {:user-id 1 :amount 30 :currency "USD"})
  (dispatch :deposit {:user-id 1 :amount 200 :currency "BTC"})
  (dispatch :deposit {:user-id 2 :amount 50 :currency "USD"})
  (dispatch :deposit {:user-id 2 :amount 10 :currency "BTC"})

  (dispatch :place-order {:user-id 1 :side :buy :amount 0.2 :ticker "BTCUSD" :price 100 :type :limit})
  (dispatch :place-order {:user-id 2 :side :sell :amount 0.2 :ticker "BTCUSD" :price 100 :type :limit})

  (def order-book (dispatch :get-order-book {:ticker "BTCUSD"})))
