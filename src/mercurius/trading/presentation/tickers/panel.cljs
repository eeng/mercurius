(ns mercurius.trading.presentation.tickers.panel
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.views.components :refer [panel]]
            [mercurius.trading.presentation.tickers.flow]))

(defn tickers-panel []
  (let [{:keys [data loading?]} (<sub [:trading/tickers])
        ticker-selected (<sub [:trading/ticker-selected])]
    [panel
     {:header "Tickers" :loading? loading?}
     (when data
       [:table.table.is-narrow.is-hoverable
        [:thead>tr
         [:th "Name"]
         [:th "Last Price"]
         [:th "Volume"]]
        [:tbody
         (for [{:keys [ticker last-price volume]} (vals data)]
           [:tr {:key ticker
                 :on-click #(>evt [:trading/ticker-selected ticker])
                 :class (when (= ticker ticker-selected) "is-selected")}
            [:td ticker]
            [:td {:align "right"} last-price]
            [:td {:align "right"} volume]])]])]))
