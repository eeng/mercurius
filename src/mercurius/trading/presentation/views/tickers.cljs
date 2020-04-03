(ns mercurius.trading.presentation.views.tickers
  (:require [mercurius.core.presentation.util.reframe :refer [<sub]]
            [mercurius.core.presentation.util.format :refer [format-money]]))

(defn tickers-panel []
  (let [{:keys [data]} (<sub [:trading/tickers])]
    [:div.panel
     [:div "Tickers"]
     (when data
       [:table
        [:thead>tr
         [:td "Name"]
         [:td "Last Price"]
         [:td "Volume"]]
        [:tbody
         (for [{:keys [ticker last-price volume]} (vals data)]
           [:tr {:key ticker}
            [:td ticker]
            [:td {:align "right"} (format-money last-price)]
            [:td {:align "right"} volume]])]])]))
