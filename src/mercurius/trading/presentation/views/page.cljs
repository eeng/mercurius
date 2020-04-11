(ns mercurius.trading.presentation.views.page
  (:require [mercurius.trading.presentation.views.tickers :refer [tickers-panel]]
            [mercurius.trading.presentation.views.order-book :refer [order-book-panel]]
            [mercurius.trading.presentation.views.trades :refer [trades-panel]]))

(defn trading-page []
  [:div.trading-page
   [:div.columns
    [:div.column.is-narrow
     [tickers-panel]]]
   [:div.columns
    [:div.column [order-book-panel]]
    [:div.column.is-5 [trades-panel]]]])
