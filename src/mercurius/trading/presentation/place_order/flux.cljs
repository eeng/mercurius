(ns mercurius.trading.presentation.place-order.flux
  (:require [re-frame.core :refer [reg-sub reg-event-fx]]
            [cljs.core.match :refer-macros [match]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db]]
            [mercurius.core.presentation.util.string :refer [parse-float]]
            [mercurius.trading.presentation.tickers.flux :refer [ticker-selected]]))

(def default-place-order-form
  {:values {:type "limit"}
   :loading? false
   :valid? false})

;;;; Subscriptions

(defn- dissoc-price-if-market-order [{:keys [type] :as order}]
  (cond-> order
    (= type :market) (dissoc :price)))

(defn- coerced-command [db]
  (-> (get-in db [:place-order-form :values])
      (update :type keyword)
      (update :amount parse-float)
      (update :price parse-float)
      (dissoc-price-if-market-order)))

(defn command-valid? [order]
  (match [order]
    [{:type :market :amount amount}] (pos? amount)
    [{:type :limit :amount amount :price price}] (and (pos? amount) (pos? price))
    :else false))

(reg-sub
 :trading/place-order-form
 (fn [{:keys [place-order-form] :as db}]
   (assoc place-order-form
          :valid? (-> (coerced-command db) (command-valid?)))))

;;;; Events

(reg-event-db
 :trading/place-order-form-changed
 (fn [db [_ form-changes]]
   (update-in db [:place-order-form :values] merge form-changes)))

(reg-event-fx
 :trading/place-order
 (fn [{:keys [db]} [_ side]]
   (let [order (-> (coerced-command db)
                   (assoc :side side :ticker (ticker-selected db)))]
     {:db (assoc-in db [:place-order-form :loading?] true)
      :socket-request {:request [:place-order order]
                       :on-success [:command-success [:place-order-form]
                                    (->> (get-in db [:place-order-form :values :type])
                                         (assoc-in default-place-order-form [:values :type]))
                                    "Order placed!"]
                       :on-failure [:command-failure [:place-order-form]]}})))
