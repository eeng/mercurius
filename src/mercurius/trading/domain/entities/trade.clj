(ns mercurius.trading.domain.entities.trade
  (:require [tick.alpha.api :as t]
            [mercurius.trading.domain.entities.order :refer [fill-order partially-filled? amount-paid currency-paid]]
            [mercurius.trading.domain.entities.order-book :refer [sort-orders-for-side]]))

(defrecord Trade [ticker price amount bid ask])

(def new-trade map->Trade)

(defn generate-trade
  "Tries to produce a trade between the bid and ask orders.
  If the bid price is greater or equal to the ask price, a trade is returned.
  Otherwise returns nil."
  [{bid-price :price bid-placed-at :placed-at remaining-bid :remaining bid-user :user-id ticker :ticker :as bid}
   {ask-price :price ask-placed-at :placed-at remaining-ask :remaining ask-user :user-id :as ask}]
  (when (and bid ask (>= bid-price ask-price) (not= bid-user ask-user))
    (let [trade-price (if (t/< bid-placed-at ask-placed-at) ask-price bid-price)
          trade-amount (min remaining-bid remaining-ask)]
      (new-trade {:price trade-price
                  :amount trade-amount
                  :ticker ticker
                  :bid (fill-order bid trade-amount)
                  :ask (fill-order ask trade-amount)}))))

(defn- keeping-partially-filled [order next-orders]
  (if (partially-filled? order)
    (cons order next-orders)
    next-orders))

(defn match-orders
  "Receives a map of bids and asks orders and finds matches between them.
  Returns a vector of the trades generated, each with the updated orders.
  If no trade is possible returns an empty vector."
  [bids asks]
  (loop [[bid & next-bids] (sort-orders-for-side :buying bids)
         [ask & next-asks] (sort-orders-for-side :selling asks)
         trades []]
    (if-let [{:keys [bid ask] :as trade} (generate-trade bid ask)]
      (recur (keeping-partially-filled bid next-bids)
             (keeping-partially-filled ask next-asks)
             (conj trades trade))
      trades)))

(defn build-transfers
  "Generates a data structure representing the transfer that need to be made between the traders."
  [{:keys [amount price]
    {buyer :user-id bid-limit-price :price :keys [ticker]} :bid
    {seller :user-id ask-limit-price :price} :ask}]
  [{:from buyer
    :to seller
    :currency (currency-paid :buy ticker)
    :transfer-amount (amount-paid :buy amount price)
    :cancel-amount (amount-paid :buy amount bid-limit-price)}
   {:from seller
    :to buyer
    :currency (currency-paid :sell ticker)
    :transfer-amount (amount-paid :sell amount price)
    :cancel-amount (amount-paid :sell amount ask-limit-price)}])

(defn calculate-price-direction [{:keys [price]} prev-price]
  (if (or (not prev-price)
          (>= price prev-price))
    :up
    :down))
