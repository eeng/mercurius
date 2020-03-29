(ns fiddles.mediator
  (:require [user :refer [system]]))

(comment
  (def dispatch (:dispatch system))

  (dispatch :deposit {:user-id 1 :amount 1000 :currency "USD"})
  (dispatch :deposit {:user-id 2 :amount 10 :currency "BTC"})

  (dispatch :place-order {:user-id 1 :side :buy :amount 3 :ticker "BTCUSD" :price 100 :type :limit})
  (dispatch :place-order {:user-id 2 :side :sell :amount 2 :ticker "BTCUSD" :price 90 :type :limit})
  (dispatch :place-order {:user-id 2 :side :sell :amount 4 :ticker "BTCUSD" :price 100 :type :limit})

  #_(dispatch :execute-trades {:ticker "BTCUSD"})
  (dispatch :get-tickers {})

  (dispatch :get-order-book {:ticker "BTCUSD" :precision "P1" :limit 10})
  (dispatch :get-wallets {:user-id 1})
  (dispatch :get-wallets {:user-id 2})
  (dispatch :calculate-monetary-base {})

  (def x (atom {:version 1 :balance 0}))

  (defn optimistic-locking [new-val current-val]
    (if (= (:version new-val) (:version current-val))
      (update new-val :version inc)
      (throw (IllegalStateException.
              (format "Stale object error. Expected version %d but got %d. Object: %s"
                      (:version current-val)
                      (:version new-val)
                      (pr-str new-val))))))

  (defn save [new-val]
    (swap! x (partial optimistic-locking new-val)))

  (save {:version 1 :balance 10})
  (save {:version 1 :balance 11}))
