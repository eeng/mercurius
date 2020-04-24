(ns mercurius.trading.presentation.page
  (:require [mercurius.trading.presentation.tickers.panel :refer [tickers-panel]]
            [mercurius.trading.presentation.balances.panel :refer [balances-panel]]
            [mercurius.trading.presentation.order-book.panel :refer [order-book-panel]]
            [mercurius.trading.presentation.trades.panel :refer [trades-panel]]
            [mercurius.trading.presentation.sidebar :refer [sidebar]]))

(defn trading-page []
  [:div.page.trading-page
   [:div.columns
    [:div.column
     [:div.columns
      [:div.column.is-4
       [tickers-panel]
       [trades-panel]]
      [:div.column
       [balances-panel]
       [order-book-panel]]]]
    [:div.column.is-3
     [sidebar]]]])
