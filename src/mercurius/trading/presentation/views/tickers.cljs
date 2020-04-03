(ns mercurius.trading.presentation.views.tickers
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.util.format :refer [format-money]]))

(defn tickers-panel []
  (let [{:keys [data]} (<sub [:trading/tickers])]
    [:div.panel
     [:div "Tickers"]
     (when data
       [:table
        [:thead>tr
         [:th "Name"]
         [:th "Last Price"]
         [:th "Volume"]]
        [:tbody
         (for [{:keys [ticker last-price volume]} (vals data)]
           [:tr {:key ticker
                 :on-click #(>evt [:trading/ticker-selected ticker])}
            [:td ticker]
            [:td {:align "right"} (format-money last-price)]
            [:td {:align "right"} volume]])]])]))
