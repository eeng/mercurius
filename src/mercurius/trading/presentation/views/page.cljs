(ns mercurius.trading.presentation.views.page
  (:require [mercurius.trading.presentation.views.tickers :refer [tickers-panel]]
            [mercurius.trading.presentation.views.order-book :refer [order-book-panel]]))

(defn trading-page []
  [:<>
   [tickers-panel]
   [order-book-panel]])
