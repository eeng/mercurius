(ns mercurius.trading.domain.use-cases.match-orders
  (:require [clojure.spec.alpha :as s]
            [mercurius.trading.domain.entities.order :as order :refer [calculate-reservation]]
            [mercurius.trading.domain.entities.ticker :refer [currencies]]
            [mercurius.trading.domain.entities.trade :refer [generate-trade]]
            [mercurius.wallets.domain.entities.wallet :refer [transfer cancel-reservation]]))

(s/def ::bid ::order/order)
(s/def ::ask ::order/order)
(s/def ::command (s/and (s/keys :req-un [::bid ::ask])
                        #(= (get-in % [:bid :ticker])
                            (get-in % [:ask :ticker]))))

(defn new-match-orders-use-case
  "Returns a use case that match bid an ask orders to see if a trade can be made.
  If a trade is made, a transfer is made between buyer and seller for each pais's currency."
  [{:keys [fetch-wallet load-wallet save-wallet]}]
  (fn [{:keys [bid ask] :as command}]
    (s/assert ::command command)
    (when-let [{:keys [ticker price amount] :as trade} (generate-trade bid ask)]
      (let [[currency1 currency2] (currencies ticker)
            [buyer seller] (map :user-id [bid ask])
            [buyer-reserv seller-reserv] (map (comp :amount calculate-reservation) [bid ask])

            buyer-src-wallet (-> (fetch-wallet buyer currency2)
                                 (cancel-reservation buyer-reserv))
            seller-dst-wallet (load-wallet seller currency2)
            exchange1-wallets (transfer buyer-src-wallet seller-dst-wallet (* price amount))

            seller-src-wallet (-> (fetch-wallet seller currency1)
                                  (cancel-reservation seller-reserv))
            buyer-dst-wallet (load-wallet buyer currency1)
            exchange2-wallets (transfer seller-src-wallet buyer-dst-wallet amount)]
        (doseq [wallet (concat exchange1-wallets exchange2-wallets)]
          (save-wallet wallet))
        trade))))
