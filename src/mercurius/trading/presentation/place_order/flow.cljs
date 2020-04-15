(ns mercurius.trading.presentation.place-order.flow
  (:require [re-frame.core :refer [reg-sub reg-event-fx]]
            [cljs.core.match :refer-macros [match]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db]]
            [mercurius.core.presentation.util.string :refer [parse-float]]
            [mercurius.trading.presentation.tickers.flow :refer [ticker-selected]]))

;;;; Subscriptions

(defn- dissoc-price-if-market-order [{:keys [type] :as order}]
  (cond-> order
    (= type :market) (dissoc :price)))

(defn- coerce-order [order]
  (-> order
      (update :type keyword)
      (update :amount parse-float)
      (update :price parse-float)
      (dissoc-price-if-market-order)))

(defn order-valid? [order]
  (match [order]
    [{:type :market :amount amount}] (pos? amount)
    [{:type :limit :amount amount :price price}] (and (pos? amount) (pos? price))
    :else false))

(reg-sub
 :trading/place-order-form
 (fn [{:keys [place-order-form]}]
   (assoc place-order-form
          :valid? (-> place-order-form :values coerce-order order-valid?))))

;;;; Events

(def default-place-order-form {:loading? false :values {:type "limit"}})

(reg-event-db
 :trading/place-order-form-changed
 (fn [db [_ form-changes]]
   (update-in db [:place-order-form :values] merge form-changes)))

(reg-event-fx
 :trading/place-order
 (fn [{:keys [db]} [_ side]]
   (let [order (-> (get-in db [:place-order-form :values])
                   (assoc :ticker (ticker-selected db) :side side)
                   (coerce-order))]
     {:db (assoc-in db [:place-order-form :loading?] true)
      :api {:request [:place-order order]
            :on-success [:trading/place-order-success]
            :on-failure [:trading/place-order-failure]}})))

(reg-event-db
 :trading/place-order-success
 (fn [{:keys [place-order-form] :as db} _]
   (->> (get-in place-order-form [:values :type])
        (assoc-in default-place-order-form [:values :type])
        (assoc db :place-order-form))))

(reg-event-fx
 :trading/place-order-failure
 (fn [{:keys [db]} [_ {:keys [type] :as error}]]
   (js/console.error error)
   {:db (assoc-in db [:place-order-form :loading?] false)
    :toast {:message (case type
                       :wallet/insufficient-balance "Insufficient balance."
                       "Unexpected error.")
            :type "is-danger faster"}}))
