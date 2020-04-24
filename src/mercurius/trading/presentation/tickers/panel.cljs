(ns mercurius.trading.presentation.tickers.panel
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.views.components :refer [panel]]
            [mercurius.trading.presentation.tickers.flow]
            [mercurius.core.presentation.util.format :refer [format-number]]))

(defn tickers-panel []
  (let [{:keys [data loading?]} (<sub [:trading/tickers])
        ticker-selected (<sub [:trading/ticker-selected])]
    [panel
     {:header "Tickers" :loading? loading?}
     (when data
       [:table.table.is-hoverable.is-fullwidth
        [:thead>tr
         [:th "Name"]
         [:th {:align "right"} "Price"]
         [:th {:align "right"} "Volume"]]
        [:tbody
         (for [{:keys [ticker last-price volume]} (vals data)]
           [:tr {:key ticker
                 :on-click #(>evt [:trading/ticker-selected ticker])
                 :class (when (= ticker ticker-selected) "is-selected")}
            [:td ticker]
            [:td {:align "right"} last-price]
            [:td {:align "right"} (format-number volume)]])]])]))
