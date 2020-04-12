(ns mercurius.trading.presentation.views.trades
  (:require [mercurius.core.presentation.util.reframe :refer [<sub]]
            [mercurius.core.presentation.util.format :refer [format-time]]
            [mercurius.core.presentation.views.components :refer [panel]]))

(defn trades-panel []
  (let [ticker (<sub [:trading/ticker-selected])
        {:keys [data loading?]} (<sub [:trading/trades ticker])]
    [panel {:header "Trades" :subheader ticker :loading? loading?}
     (if (seq data)
       [:table.table.is-narrow.is-fullwidth
        [:thead>tr
         [:th "Time"]
         [:th {:align "right"} "Price"]
         [:th {:align "right"} "Amount"]]
        [:tbody
         (for [{:keys [id price amount created-at]} data]
           [:tr {:key id}
            [:td (format-time created-at)]
            [:td {:align "right"} price]
            [:td {:align "right"} amount]])]]
       [:div "Nothing to show here."])]))