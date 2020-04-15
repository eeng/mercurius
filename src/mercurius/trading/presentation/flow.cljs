(ns mercurius.trading.presentation.flow
  (:require [re-frame.core :refer [reg-sub-raw reg-sub reg-event-fx]]
            [reagent.ratom :refer [reaction]]
            [cljs.core.match :refer-macros [match]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db >evt]]
            [mercurius.core.presentation.util.string :refer [parse-float]]))

;;;; Events 

(reg-event-fx
 :trading/get-tickers
 (fn [_ _]
   {:api {:request [:get-tickers]
          :on-success [:ok-response [:tickers]]
          :on-failure [:bad-response [:tickers]]}}))

(reg-event-db
 :trading/ticker-updated
 (fn [db [_ {:keys [ticker] :as ticker-stats}]]
   (cond-> db
     (get-in db [:tickers :data ticker])
     (assoc-in [:tickers :data ticker] ticker-stats))))

(reg-event-db
 :trading/ticker-selected
 (fn [db [_ ticker]]
   (assoc db :ticker-selected ticker)))

(reg-event-fx
 :trading/get-order-book
 (fn [_ [_ filters]]
   {:api {:request [:get-order-book filters]
          :on-success [:ok-response [:order-book]]
          :on-failure [:bad-response [:order-book]]}}))

(defn- ticker-selected [{:keys [tickers] :as db}]
  (or (:ticker-selected db) (-> tickers :data keys first)))

(defn- order-book-filters [{:keys [order-book-precision] :as db}]
  {:ticker (ticker-selected db)
   :precision order-book-precision
   :limit 20})

(reg-event-fx
 :trading/refresh-order-book
 (fn [{:keys [db]} _]
   {:dispatch [:trading/get-order-book (order-book-filters db)]}))

(def precisions ["P0" "P1" "P2" "P3" "P4"])

(defn calculate-new-precision [{:keys [order-book-precision]} direction]
  (->> order-book-precision
       (.indexOf precisions)
       direction
       (get precisions)))

(reg-event-db
 :trading/increase-book-precision
 (fn [db _]
   (if-let [new-precision (calculate-new-precision db dec)]
     (assoc db :order-book-precision new-precision)
     db)))

(reg-event-db
 :trading/decrease-book-precision
 (fn [db _]
   (if-let [new-precision (calculate-new-precision db inc)]
     (assoc db :order-book-precision new-precision)
     db)))

(reg-event-fx
 :trading/get-trades
 (fn [_ [_ ticker]]
   {:api {:request [:get-trades {:ticker ticker}]
          :on-success [:ok-response [:trades]]
          :on-failure [:bad-response [:trades]]}}))

(reg-event-db
 :trading/trade-made
 (fn [db [_ trade]]
   (update-in db [:trades :data] (comp (partial take 100) conj) trade)))

(reg-event-db
 :trading/place-order-form-changed
 (fn [db [_ form-changes]]
   (update-in db [:place-order-form :values] merge form-changes)))

(def default-place-order-form {:loading? false :values {:type "limit"}})

(defn- dissoc-price-if-market-order [{:keys [type] :as order}]
  (cond-> order
    (= type :market) (dissoc :price)))

(defn- coerce-order [order]
  (-> order
      (update :type keyword)
      (update :amount parse-float)
      (update :price parse-float)
      (dissoc-price-if-market-order)))

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
                       :wallet/insufficient-balance "Insufficient balance"
                       "Unexpected error")
            :type "is-danger faster"}}))

;;;; Subscriptions

(reg-sub-raw
 :trading/tickers
 (fn [app-db _]
   (>evt [:trading/get-tickers])
   (reaction (get @app-db :tickers {:loading? true}))))

(reg-sub
 :trading/ticker-selected
 (fn [db _]
   (ticker-selected db)))

(reg-sub
 :trading/ticker-selected-currencies
 (fn [db _]
   (let [ticker (ticker-selected db)]
     (if ticker
       [(subs ticker 0 3) (subs ticker 3 6)]
       []))))

(reg-sub
 :trading/order-book-filters
 (fn [db _]
   (order-book-filters db)))

(reg-sub-raw
 :trading/order-book
 (fn [app-db [_ filters]]
   (>evt [:trading/get-order-book filters])
   (reaction (get @app-db :order-book {:loading? true}))))

(reg-sub
 :trading/cant-increase-book-precision
 (fn [{:keys [order-book-precision]} _]
   (= order-book-precision (first precisions))))

(reg-sub
 :trading/cant-decrease-book-precision
 (fn [{:keys [order-book-precision]} _]
   (= order-book-precision (last precisions))))

(reg-sub-raw
 :trading/trades
 (fn [app-db [_ ticker]]
   (>evt [:trading/get-trades ticker])
   (reaction (get @app-db :trades {:loading? true}))))

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
