(ns fiddles.transactions
  (:require [user :refer [system]]))

(comment
  (def dispatch (:dispatch system))
  (dispatch :deposit {:user-id 1 :amount 100 :currency "USD"})
  (->> #(dispatch :transfer {:from 1 :to 2 :transfer-amount 1 :currency "USD"})
       (repeat 100)
       (apply pcalls)
       (dorun))
  (let [src-balance (:balance (dispatch :get-wallet {:user-id 1 :currency "USD"}))
        dst-balance (:balance (dispatch :get-wallet {:user-id 2 :currency "USD"}))]
    [src-balance dst-balance (+ src-balance dst-balance)]))
