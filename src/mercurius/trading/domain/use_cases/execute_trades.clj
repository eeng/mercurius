(ns mercurius.trading.domain.use-cases.execute-trades
  (:require [clojure.spec.alpha :as s]
            [mercurius.trading.domain.entities.order :as order :refer [partially-filled?]]
            [mercurius.trading.domain.entities.trade :refer [match-orders build-transfers]]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.wallets.domain.entities.wallet :refer [transfer cancel-reservation]]
            [taoensso.timbre :as log]))

(s/def ::ticker ::ticker/ticker)
(s/def ::command (s/and (s/keys :req-un [::ticker])))

(defn- make-transfer [{:keys [fetch-wallet load-wallet save-wallet]}
                      {:keys [from to currency transfer-amount cancel-amount]}]
  (let [src (-> (fetch-wallet from currency)
                (cancel-reservation cancel-amount))
        dst (load-wallet to currency)
        wallets (transfer src dst transfer-amount)]
    (doseq [wallet wallets]
      (save-wallet wallet))))

(defn- update-order-book [{:keys [update-order remove-order]} order]
  (if (partially-filled? order)
    (update-order order)
    (remove-order order)))

(defn new-execute-trades-use-case
  "Returns a use case that match bid an ask orders to discover trades, and executes them.
  For each trade, a transfer is made between buyer and seller for each pais's currency.
  Finally the order book is updated."
  [{:keys [get-bids-asks] :as deps}]
  (fn [{:keys [ticker] :as command}]
    (s/assert ::command command)
    (let [{:keys [bids asks]} (get-bids-asks ticker)
          trades (match-orders bids asks)]
      (doseq [{:keys [bid ask] :as trade} trades]
        (log/debug "Trade made!" trade)
        (doseq [transfer (build-transfers trade)]
          (make-transfer deps transfer))
        (doseq [order [bid ask]]
          (update-order-book deps order)))
      trades)))
