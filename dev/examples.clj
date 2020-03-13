(ns examples
  (:require [integrant.repl.state :refer [system]]
            [mercurius.core.controllers.mediator :refer [dispatch]]))

(comment
  (def mediator (:mediator system))
  (dispatch mediator {:type :wallets/deposit :user-id 1 :amount 100 :currency "USD"})
  (dispatch mediator {:type :wallets/withdraw :user-id 1 :amount 30 :currency "USD"})
  (dispatch mediator {:type :wallets/deposit :user-id 2 :amount 50 :currency "USD"}))
