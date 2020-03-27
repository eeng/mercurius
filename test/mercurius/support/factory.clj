(ns mercurius.support.factory
  (:require [mercurius.wallets.domain.entities.wallet :refer [new-wallet]]
            [mercurius.trading.domain.entities.order :refer [new-order]]
            [mercurius.trading.domain.entities.trade :refer [new-trade]]))

(defn- build-with [entity-fn defaults]
  (fn [& [args]]
    (entity-fn (merge defaults args))))

(def build-wallet
  (build-with new-wallet {:currency "USD" :user-id 1}))

(def build-order
  (build-with new-order {:user-id 1 :type :limit :side :buy :amount 0.2 :ticker "BTCUSD" :price 100}))

(def build-trade
  (build-with new-trade {:ticker "BTCUSD" :amount 1M :price 100.0}))

(comment
  (build-wallet)
  (build-wallet {:balance 5}))
