(ns mercurius.trading.presentation.trades.panel
  (:require [mercurius.core.presentation.util.reframe :refer [<sub]]
            [mercurius.core.presentation.util.format :refer [format-time]]
            [mercurius.core.presentation.views.components :refer [panel]]
            [mercurius.trading.presentation.trades.flow]))

(defn- direction-icon [direction]
  (case direction
    :up [:span.icon.is-small.has-text-success
         [:i.fas.fa-chevron-up]]
    :down [:span.icon.is-small.has-text-danger
           [:i.fas.fa-chevron-down]]))

(defn trades-panel []
  (let [ticker (<sub [:trading/ticker-selected])
        {:keys [data loading?]} (<sub [:trading/trades ticker])]
    [panel {:header "Trades" :subheader ticker :loading? loading? :class "clipped"}
     (if (seq data)
       [:table.table.is-narrow.is-fullwidth
        [:thead>tr
         [:th.is-narrow]
         [:th "Time"]
         [:th {:align "right"} "Price"]
         [:th {:align "right"} "Amount"]]
        [:tbody
         (for [{:keys [id price amount created-at direction]} data]
           [:tr {:key id}
            [:td.is-narrow [direction-icon direction]]
            [:td (format-time created-at)]
            [:td {:align "right"} price]
            [:td {:align "right"} amount]])]]
       [:div "Nothing to show here."])]))
