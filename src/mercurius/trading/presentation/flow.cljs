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

;;;; Subscriptions

(reg-sub-raw
 :trading/tickers
 (fn [app-db _]
   (remote-query-sub app-db [:get-tickers] [:tickers])))

(reg-sub
 :trading/order-book-filters
 (fn [{:keys [current-ticker]} _]
   (when current-ticker
     {:ticker current-ticker :precision "P0" :limit 10})))

(reg-sub-raw
 :trading/order-book
 (fn [app-db [_ filters]]
   (remote-query-sub app-db [:get-order-book filters] [:order-book])))
