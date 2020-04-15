(ns mercurius.trading.presentation.balances.flow
  (:require [reagent.ratom :refer [reaction]]
            [re-frame.core :refer [reg-sub-raw reg-event-fx]]
            [mercurius.core.presentation.util.reframe :refer [>evt]]))

;;;; Subscriptions

(reg-sub-raw
 :trading/wallets
 (fn [app-db _]
   (>evt [:trading/get-wallets])
   (reaction (get @app-db :wallets {:loading? true}))))

;;;; Events

(reg-event-fx
 :trading/get-wallets
 (fn [_ _]
   {:api {:request [:get-wallets]
          :on-success [:ok-response [:wallets]]
          :on-failure [:bad-response [:wallets]]}}))
