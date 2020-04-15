(ns mercurius.trading.presentation.order-book.flow
  (:require [re-frame.core :refer [reg-sub-raw reg-sub reg-event-fx]]
            [reagent.ratom :refer [reaction]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db >evt]]
            [mercurius.trading.presentation.tickers.flow :refer [ticker-selected]]))

(def precisions ["P0" "P1" "P2" "P3" "P4"])

(defn- order-book-filters [{:keys [order-book-precision] :as db}]
  {:ticker (ticker-selected db)
   :precision order-book-precision
   :limit 20})

;;;; Subscriptions

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

;;;; Events 

(reg-event-fx
 :trading/get-order-book
 (fn [_ [_ filters]]
   {:api {:request [:get-order-book filters]
          :on-success [:ok-response [:order-book]]
          :on-failure [:bad-response [:order-book]]}}))

(reg-event-fx
 :trading/refresh-order-book
 (fn [{:keys [db]} _]
   {:dispatch [:trading/get-order-book (order-book-filters db)]}))

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
