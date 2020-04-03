(ns mercurius.trading.presentation.flow
  (:require [re-frame.core :refer [reg-sub reg-event-db]]))

;;;; Events 

(reg-event-db
 :trading/ticker-updated
 (fn [db ev]
   (js/console.log "In event handler" ev)
   db))

;;;; Subscriptions

(reg-sub
 :trading/tickers
 (fn [db _]
   [{:ticker "BTCUSD"}]))
