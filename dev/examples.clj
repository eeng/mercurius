(ns examples
  (:require [user :refer [system]]
            [mercurius.core.adapters.controllers.mediator :as m]))

(comment
  (def dispatch (partial m/dispatch (:mediator system)))

  (dispatch :deposit {:user-id 1 :amount 500 :currency "USD"})
  (dispatch :deposit {:user-id 2 :amount 10 :currency "BTC"})

  (dispatch :place-order {:user-id 1 :side :buy :amount 3 :ticker "BTCUSD" :price 100 :type :limit})
  (dispatch :place-order {:user-id 2 :side :sell :amount 3 :ticker "BTCUSD" :price 100 :type :limit})

  (dispatch :get-wallets {:user-id 1})
  (dispatch :get-wallets {:user-id 2})
  (dispatch :get-order-book {:ticker "BTCUSD"}))
