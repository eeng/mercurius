(ns mercurius.support.factory
  (:require [mercurius.wallets.domain.entities.wallet :refer [new-wallet]]
            [mercurius.trading.domain.entities.order :refer [new-order]]
            [mercurius.trading.domain.entities.trade :refer [new-trade]]))

(def last-id (atom 0))
(defn- next-id! []
  (swap! last-id inc))

(defn- build-with [entity-fn defaults-fn]
  (fn [& [args]]
    (let [defaults (if (fn? defaults-fn)
                     (defaults-fn args)
                     defaults-fn)]
      (entity-fn (merge defaults args)))))

(def build-wallet
  (build-with new-wallet {:currency "USD" :user-id 1}))

(def build-order
  (build-with new-order
              (fn [_]
                {:user-id (next-id!)
                 :type :limit
                 :side :buy
                 :amount 0.2
                 :ticker "BTCUSD"
                 :price 100})))

(def build-trade
  (build-with new-trade {:ticker "BTCUSD" :amount 1M :price 100.0}))

(comment
  (build-wallet)
  (build-wallet {:balance 5})
  (build-order))
