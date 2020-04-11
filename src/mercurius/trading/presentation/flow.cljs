(ns mercurius.trading.presentation.flow
  (:require [re-frame.core :refer [reg-sub-raw reg-sub reg-event-fx]]
            [reagent.ratom :refer [reaction]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db >evt]]))

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

(reg-event-db
 :trading/trade-made
 (fn [db [_ trade]]
   (update db :trades (comp (partial take 100) conj) trade)))

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

(reg-sub
 :trading/trades
 (fn [{:keys [trades]}]
   trades))
