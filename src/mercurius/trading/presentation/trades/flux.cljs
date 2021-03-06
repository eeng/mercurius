(ns mercurius.trading.presentation.trades.flux
  (:require [re-frame.core :refer [reg-sub-raw reg-event-fx]]
            [reagent.ratom :refer [reaction]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db >evt]]
            [mercurius.trading.presentation.tickers.flux :refer [ticker-selected]]
            [mercurius.accounts.presentation.login.flux :refer [current-user-id]]))

;;;; Subscriptions

(reg-sub-raw
 :trading/trades
 (fn [app-db [_ ticker]]
   (>evt [:trading/get-trades ticker])
   (reaction (get @app-db :trades {:loading? true}))))

;;;; Events 

(reg-event-fx
 :trading/get-trades
 (fn [{:keys [db]} [_ ticker]]
   (when ticker
     {:socket-request {:request [:get-trades {:ticker ticker}]
                       :on-success [:query-success [:trades]]
                       :on-failure [:query-failure [:trades]]}
      :socket-subscribe [{:topic (str "trade-executed." ticker)
                          :on-message [:trading/trade-executed]}
                         {:topic (str "trade-executed." (current-user-id db))
                          :on-message [:trading/my-trade-executed]}]})))

(reg-event-db
 :trading/trade-executed
 (fn [db [_ trade]]
   ; Just in case we receive the event after selecting other ticker
   (if (= (:ticker trade) (ticker-selected db))
     (update-in db [:trades :data] (comp (partial take 100) conj) trade)
     db)))

(reg-event-fx
 :trading/my-trade-executed
 (fn [_ [_ {:keys [amount ticker]}]]
   {:toast {:message (str "Trade for " amount " " ticker " executed!")
            :type "is-success faster"
            :duration 7000}}))
