(ns mercurius.trading.domain.use-cases.execute-trades
  (:require [clojure.spec.alpha :as s]
            [mercurius.trading.domain.entities.order :as order]
            [mercurius.trading.domain.entities.trade :refer [match-orders build-transfers]]
            [mercurius.wallets.domain.entities.wallet :refer [transfer cancel-reservation]]))

(s/def ::bids (s/coll-of ::order/order))
(s/def ::asks (s/coll-of ::order/order))
(s/def ::command (s/and (s/keys :req-un [::bids ::asks])
                        #(= 1 (->> (map :ticker %) distinct count))))

(defn- make-transfer [{:keys [from to currency transfer-amount cancel-amount]}
                      {:keys [fetch-wallet load-wallet save-wallet]}]
  (let [src (-> (fetch-wallet from currency)
                (cancel-reservation cancel-amount))
        dst (load-wallet to currency)
        wallets (transfer src dst transfer-amount)]
    (doseq [wallet wallets]
      (save-wallet wallet))))

(defn new-execute-trades-use-case
  "Returns a use case that match bid an ask orders to see if a trade can be made.
  If a trade is made, a transfer is made between buyer and seller for each pais's currency."
  [deps]
  (fn [{:keys [bids asks] :as command}]
    (s/assert ::command command)
    (let [trades (match-orders bids asks)]
      (doseq [trade trades
              transfer (build-transfers trade)]
        (make-transfer transfer deps))
      trades)))
