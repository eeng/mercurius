(ns mercurius.trading.presentation.balances.panel
  (:require [mercurius.core.presentation.util.reframe :refer [<sub]]
            [mercurius.core.presentation.views.components :refer [panel]]
            [mercurius.trading.presentation.balances.flow]))

(defn balances-panel []
  (let [{:keys [loading? data]} (<sub [:trading/wallets])]
    [panel {:header "Balances" :loading? loading?}
     (if (seq data)
       [:table.table
        [:tbody
         (for [{:keys [currency balance reserved]} data]
           [:tr {:key currency}
            [:td {:style {:vertical-align "middle"}} currency]
            [:td {:align "right"}
             [:div balance]
             (when (pos? reserved)
               [:div.m-l-xs.has-text-grey-light reserved])]])]]
       [:div "No wallets created yet."])]))
