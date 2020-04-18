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
    (def u3 (:id (find-by-username user-repo "user3")))

    (dispatch :deposit {:user-id u1 :amount 10000 :currency "USD"})
    (dispatch :deposit {:user-id u2 :amount 10000 :currency "USD"})
    (dispatch :deposit {:user-id u3 :amount 100 :currency "BTC"})

    (dispatch :place-order {:user-id u2 :side :buy :amount 1.5 :ticker "BTCUSD" :price 490 :type :limit})
    (dispatch :place-order {:user-id u2 :side :buy :amount 3 :ticker "BTCUSD" :price 489.1 :type :limit})
    (dispatch :place-order {:user-id u3 :side :sell :amount 0.5 :ticker "BTCUSD" :price 510 :type :limit})
    (dispatch :place-order {:user-id u3 :side :sell :amount 3 :ticker "BTCUSD" :price 512.3 :type :limit})
    (dispatch :place-order {:user-id u3 :side :sell :amount 1 :ticker "BTCUSD" :price 515 :type :limit}))

  ; Make a trade
  (let [dispatch (:use-cases/dispatch system)]
    (dispatch :place-order {:user-id u2 :side :buy :amount 0.1 :ticker "BTCUSD" :price 510 :type :limit})))
