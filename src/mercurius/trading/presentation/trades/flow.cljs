(ns mercurius.trading.presentation.trades.flow
  (:require [re-frame.core :refer [reg-sub-raw reg-event-fx]]
            [reagent.ratom :refer [reaction]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db >evt]]))

;;;; Subscriptions

(reg-sub-raw
 :trading/trades
 (fn [app-db [_ ticker]]
   (>evt [:trading/get-trades ticker])
   (reaction (get @app-db :trades {:loading? true}))))

;;;; Events 

(reg-event-fx
 :trading/get-trades
 (fn [_ [_ ticker]]
   (when ticker
     {:api {:request [:get-trades {:ticker ticker}]
            :on-success [:query-success [:trades]]
            :on-failure [:query-failure [:trades]]}
      :socket-subscribe {:topic (str "trade-executed." ticker)
                         :on-message [:trading/trade-executed]}})))

(reg-event-db
 :trading/trade-executed
 (fn [db [_ trade]]
   (update-in db [:trades :data] (comp (partial take 100) conj) trade)))
