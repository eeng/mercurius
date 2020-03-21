(ns mercurius.trading.domain.use-cases.execute-trades
  (:require [clojure.spec.alpha :as s]
            [mercurius.trading.domain.entities.order :as order :refer [partially-filled?]]
            [mercurius.trading.domain.entities.trade :refer [match-orders build-transfers]]
            [mercurius.wallets.domain.entities.wallet :refer [transfer cancel-reservation]]
            [taoensso.timbre :as log]))

(s/def ::bids (s/coll-of ::order/order))
(s/def ::asks (s/coll-of ::order/order))
(s/def ::command (s/and (s/keys :req-un [::bids ::asks])
                        #(= 1 (->> (map :ticker %) distinct count))))

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
  "Returns a use case that match bid an ask orders to find trades, and executes them.
  For each trade, a transfer is made between buyer and seller for each pais's currency.
  Finally the order book is updated."
  [deps]
  (fn [{:keys [bids asks] :as command}]
    (s/assert ::command command)
    (let [trades (match-orders bids asks)]
      (doseq [{:keys [bid ask] :as trade} trades]
        (log/info "Trade made!" trade)
        (doseq [transfer (build-transfers trade)]
          (make-transfer deps transfer))
        (doseq [order [bid ask]]
          (update-order-book deps order)))
      trades)))
