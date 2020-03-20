(ns mercurius.trading.domain.entities.order
  (:require [clojure.spec.alpha :as s]
            [mercurius.util.uuid :refer [uuid]]
            [mercurius.trading.domain.entities.ticker :as ticker :refer [first-currency last-currency]]
            [mercurius.accounts.domain.entities.user :as user]
            [tick.alpha.api :as t]))

;; TODO many fields duplicated in the command, and the new-order should be spec to return ::order
(s/def ::id string?)
(s/def ::user-id ::user/id)
(s/def ::type #{:market :limit})
(s/def ::side #{:buy :sell})
(s/def ::ticker ::ticker/ticker)
(s/def ::amount (s/and number? pos?))
(s/def ::price (s/and number? pos?))
(s/def ::order (s/keys :req-un [::id ::user-id ::type ::side ::ticker ::amount ::price ::placed-at]))

(defrecord Order [id user-id type side ticker amount price placed-at])

(defn new-order [fields]
  (let [defaults {:id (uuid) :placed-at (t/now)}]
    (->> fields (merge defaults) map->Order)))

(defn reserve-currency
  "Returns the pair's currency that should be used for reservations.
  I.e., when buying BTCUSD, we should reserve USD. When selling instead, we should reserve BTC."
  [side ticker]
  (s/assert ::ticker/ticker ticker)
  (case side
    :buy (last-currency ticker)
    :sell (first-currency ticker)))

(defn reserve-amount
  "Calculates the amount to reserve in the wallet.
  E.g., when buying 0.2 BTCUSD at a price of 1000, we should reserve 100 USD.
  but when selling, 0.2 BTC should be reserved."
  [side amount price]
  (case side
    :buy (* amount price)
    :sell amount))

(defn calculate-reservation
  "Calculates the amount and currency to reserve for an order."
  [{:keys [side amount ticker price]}]
  {:amount (reserve-amount side amount price)
   :currency (reserve-currency side ticker)})
