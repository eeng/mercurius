(ns fiddles.seed
  (:require [user :refer [system reset]]
            [mercurius.accounts.domain.repositories.user-repository :refer [find-by-username]]))

(comment
  (do
    (reset)
    (def dispatch (:use-cases/dispatch system))
    (def user-repo (:adapters/user-repo system))
    (def u1 (:id (find-by-username user-repo "user1")))
    (def u2 (:id (find-by-username user-repo "user2")))

    (dispatch :deposit {:user-id u1 :amount 100000 :currency "USD"})
    (dispatch :deposit {:user-id u2 :amount 100 :currency "BTC"})

    (dispatch :place-order {:user-id u1 :side :buy :amount 1.5 :ticker "BTCUSD" :price 4900 :type :limit})
    (dispatch :place-order {:user-id u1 :side :buy :amount 3 :ticker "BTCUSD" :price 4890.1 :type :limit})
    (dispatch :place-order {:user-id u2 :side :sell :amount 0.5 :ticker "BTCUSD" :price 5100 :type :limit})
    (dispatch :place-order {:user-id u2 :side :sell :amount 3 :ticker "BTCUSD" :price 5120.3 :type :limit})
    (dispatch :place-order {:user-id u2 :side :sell :amount 1 :ticker "BTCUSD" :price 5150 :type :limit}))

  ; Make a trade
  (let [dispatch (:use-cases/dispatch system)]
    (dispatch :place-order {:user-id u1 :side :buy :amount 0.1 :ticker "BTCUSD" :price 5100 :type :limit})))
