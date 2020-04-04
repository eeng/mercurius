(ns mercurius.trading.presentation.flow
  (:require [re-frame.core :refer [reg-sub-raw reg-sub]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db]]
            [mercurius.core.presentation.flow :refer [remote-query-sub]]))

;;;; Events 

(reg-event-db
 :trading/ticker-updated
 (fn [db [_ {:keys [ticker] :as ticker-stats}]]
   (cond-> db
     (get-in db [:tickers :data ticker])
     (assoc-in [:tickers :data ticker] ticker-stats))))

(reg-event-db
 :trading/ticker-selected
 (fn [db [_ ticker]]
   (assoc db :current-ticker ticker)))

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

;;;; Subscriptions

(reg-sub-raw
 :trading/tickers
 (fn [app-db _]
   (remote-query-sub app-db [:get-tickers] [:tickers])))

(reg-sub
 :trading/order-book-filters
 (fn [{:keys [current-ticker order-book-precision]} _]
   (when current-ticker
     {:ticker current-ticker
      :precision order-book-precision
      :limit 20})))

(reg-sub-raw
 :trading/order-book
 (fn [app-db [_ filters]]
   (remote-query-sub app-db [:get-order-book filters] [:order-book])))

(reg-sub
 :trading/cant-increase-book-precision
 (fn [{:keys [order-book-precision]} _]
   (= order-book-precision (first precisions))))

(reg-sub
 :trading/cant-decrease-book-precision
 (fn [{:keys [order-book-precision]} _]
   (= order-book-precision (last precisions))))
