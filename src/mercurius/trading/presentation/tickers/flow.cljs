(ns mercurius.trading.presentation.tickers.flow
  (:require [re-frame.core :refer [reg-sub-raw reg-sub reg-event-fx]]
            [reagent.ratom :refer [reaction]]
            [mercurius.core.presentation.util.reframe :refer [reg-event-db >evt]]))

;;;; Subscriptions

(reg-sub-raw
 :trading/tickers
 (fn [app-db _]
   (>evt [:trading/get-tickers])
   (reaction (get @app-db :tickers {:loading? true}))))

(defn ticker-selected [{:keys [tickers] :as db}]
  (or (:ticker-selected db) (-> tickers :data keys first)))

(reg-sub
 :trading/ticker-selected
 (fn [db _]
   (ticker-selected db)))

(reg-sub
 :trading/ticker-selected-currencies
 (fn [db _]
   (let [ticker (ticker-selected db)]
     (if ticker
       [(subs ticker 0 3) (subs ticker 3 6)]
       []))))

;;;; Events 

(reg-event-fx
 :trading/get-tickers
 (fn [_ _]
   {:socket-request {:request [:get-tickers]
                     :on-success [:query-success [:tickers]]
                     :on-failure [:query-failure [:tickers]]}
    :socket-subscribe {:topic "ticker-updated.*"
                       :on-message [:trading/ticker-updated]}}))

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
