(ns mercurius.trading.presentation.flow
  (:require [re-frame.core :refer [reg-sub-raw]]
            [mercurius.core.presentation.util :refer [reg-event-db]]
            [mercurius.core.presentation.flow :refer [remote-query-sub]]))

;;;; Events 

(reg-event-db
 :trading/ticker-updated
 (fn [db ev]
   db))

;;;; Subscriptions

(reg-sub-raw
 :trading/tickers
 (fn [app-db _]
   (remote-query-sub app-db [:get-tickers] [:tickers])))
