(ns mercurius.trading.presentation.views.trades
  (:require [mercurius.core.presentation.util.reframe :refer [<sub]]
            [mercurius.core.presentation.views.components :refer [panel]]))

(defn trades-panel []
  (let [trades (<sub [:trading/trades])]
    [panel {:header "Trades"}
     (if (seq trades)
       [:table.table.is-narrow.is-fullwidth
        [:thead>tr
         [:th "Time"]
         [:th {:align "right"} "Price"]
         [:th {:align "right"} "Amount"]]
        [:tbody
         (for [{:keys [id price amount]} trades]
           [:tr {:key id}
            [:td  "TODO time"]
            [:td {:align "right"} price]
            [:td {:align "right"} amount]])]]
       [:div "Nothing to show here."])]))
