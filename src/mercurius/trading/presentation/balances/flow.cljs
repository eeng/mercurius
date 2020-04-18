(ns mercurius.trading.presentation.balances.flow
  (:require [reagent.ratom :refer [reaction]]
            [re-frame.core :refer [reg-sub-raw reg-event-fx]]
            [mercurius.core.presentation.util.reframe :refer [>evt reg-event-db]]
            [mercurius.util.collections :refer [insert-or-replace-by]]))

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
          :on-success [:query-success [:wallets]]
          :on-failure [:query-failure [:wallets]]}}))

(reg-event-db
 :trading/wallet-changed
 (fn [db [_ event-data]]
   (update-in db
              [:wallets :data]
              (partial insert-or-replace-by :currency event-data))))
