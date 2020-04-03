(ns mercurius.trading.presentation.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :initialized?
 (fn [db _]
   (:ws-connected? db)))

(reg-sub
 :tickers
 (fn [db _]
   [{:ticker "BTCUSD"}]))
