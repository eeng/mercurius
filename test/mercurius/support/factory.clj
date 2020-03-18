(ns mercurius.support.factory
  (:require [mercurius.wallets.domain.entities.wallet :refer [new-wallet]]
            [mercurius.trading.domain.entities.order :refer [new-order]]))

(defn- build-with [entity-fn defaults]
  (fn [& [args]]
    (entity-fn (merge defaults args))))

(def build-wallet
  (build-with new-wallet {:currency "USD" :user-id 1}))

(def build-order
  (build-with new-order {:user-id 1 :type :limit :side :buy :amount 0.2 :ticker "BTCUSD" :price 100}))

(comment
  (build-wallet)
  (build-wallet {:balance 5}))
