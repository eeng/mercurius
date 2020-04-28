(ns mercurius.core.configuration.seed
  (:require [mercurius.accounts.domain.repositories.user-repository :refer [add-user]]))

(defn- create-users [{user-repo :adapters/user-repo}]
  (->> [{:id "aacbf0e46-5e80-4cc7-8cb9-3e9d3af84108" :username "user1" :password "secret"}
        {:id "182a346c-58b3-4c33-a6c0-4d5eb08e5d45" :username "user2" :password "secret"}
        {:id "5aa6ded2-0b96-41eb-95ca-1c23416fbce2" :username "user3" :password "secret"}]
       (map (comp :id (partial add-user user-repo)))))

(defn seed [system]
  (let [dispatch (:use-cases/dispatch system)
        [u1 u2 u3] (create-users system)]

    (dispatch :deposit {:user-id u1 :amount 100000 :currency "USD"})
    (dispatch :deposit {:user-id u2 :amount 100 :currency "BTC"})
    (dispatch :deposit {:user-id u3 :amount 100000 :currency "USD"})

    (dispatch :place-order {:user-id u3 :side :buy :amount 1.5 :ticker "BTCUSD" :price 4901 :type :limit})
    (dispatch :place-order {:user-id u3 :side :buy :amount 3 :ticker "BTCUSD" :price 4895.1 :type :limit})
    (dispatch :place-order {:user-id u2 :side :sell :amount 0.5 :ticker "BTCUSD" :price 5102 :type :limit})
    (dispatch :place-order {:user-id u2 :side :sell :amount 3 :ticker "BTCUSD" :price 5120.3 :type :limit})
    (dispatch :place-order {:user-id u2 :side :sell :amount 1 :ticker "BTCUSD" :price 5153 :type :limit})

    :seeded))
