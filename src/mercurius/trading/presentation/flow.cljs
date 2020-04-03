(ns mercurius.trading.presentation.flow
  (:require [re-frame.core :refer [reg-sub-raw]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db]]
            [mercurius.core.presentation.flow :refer [remote-query-sub]]))

;;;; Events 

(reg-event-db
 :trading/ticker-updated
 (fn [db [_ {:keys [ticker] :as ticker-stats}]]
   (cond-> db
     (get-in db [:tickers :data ticker])
     (assoc-in [:tickers :data ticker] ticker-stats))))

;;;; Subscriptions

(reg-sub-raw
 :trading/tickers
 (fn [app-db _]
   (remote-query-sub app-db [:get-tickers] [:tickers])))
