(ns mercurius.trading.domain.entities.order
  (:require [clojure.spec.alpha :as s]
            [mercurius.util.uuid :refer [uuid]]
            [mercurius.trading.domain.entities.ticker :as ticker :refer [pairs]]
            [tick.alpha.api :as t]))

(defrecord Order [id user-id type side ticker amount price])

(defn new-order [fields]
  (map->Order (assoc fields :id (uuid) :placed-at (t/now))))

(defn reserve-currency
  "Returns the pair's currency that should be used for reservations.
  I.e., when buying BTCUSD, we should reserve USD. When selling instead, we should reserve BTC."
  [side ticker]
  (s/assert ::ticker/ticker ticker)
  (case side
    :buy (get-in pairs [ticker :last-currency])
    :sell (get-in pairs [ticker :first-currency])))

(defn reserve-amount
  "Calculates the amount to reserve in the wallet.
  E.g., when buying 0.2 BTCUSD at a price of 1000, we should reserve 100 USD.
  but when selling, 0.2 BTC should be reserved."
  [side amount price]
  (case side
    :buy (* amount price)
    :sell amount))

(defn reserve-money [side amount ticker price]
  {:amount (reserve-amount side amount price)
   :currency (reserve-currency side ticker)})
