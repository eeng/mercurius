(ns mercurius.core.configuration.seed
  (:require [mercurius.accounts.domain.repositories.user-repository :refer [find-by-username]]))

(defn seed [system]
  (let [dispatch (:use-cases/dispatch system)
        user-repo (:adapters/user-repo system)
        u1 (:id (find-by-username user-repo "user1"))
        u2 (:id (find-by-username user-repo "user2"))
        u3 (:id (find-by-username user-repo "user3"))]

    (dispatch :deposit {:user-id u1 :amount 100000 :currency "USD"})
    (dispatch :deposit {:user-id u2 :amount 100000 :currency "USD"})
    (dispatch :deposit {:user-id u3 :amount 100 :currency "BTC"})

    (dispatch :place-order {:user-id u2 :side :buy :amount 1.5 :ticker "BTCUSD" :price 4901 :type :limit})
    (dispatch :place-order {:user-id u2 :side :buy :amount 3 :ticker "BTCUSD" :price 4895.1 :type :limit})
    (dispatch :place-order {:user-id u3 :side :sell :amount 0.5 :ticker "BTCUSD" :price 5102 :type :limit})
    (dispatch :place-order {:user-id u3 :side :sell :amount 3 :ticker "BTCUSD" :price 5120.3 :type :limit})
    (dispatch :place-order {:user-id u3 :side :sell :amount 1 :ticker "BTCUSD" :price 5153 :type :limit})))
