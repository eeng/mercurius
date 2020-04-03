(ns mercurius.trading.presentation.events
  (:require [re-frame.core :refer [reg-event-db]]))

(reg-event-db
 :trading/ticker-updated
 (fn [db ev]
   (js/console.log "In event handler" ev)
   db))
