(ns mercurius.core.presentation.app
  (:require [mercurius.core.presentation.util.reframe :refer [<sub]]
            [mercurius.core.presentation.flow]
            [mercurius.trading.presentation.flow]
            [mercurius.trading.presentation.views.tickers :refer [tickers-panel]]
            [mercurius.trading.presentation.views.order-book :refer [order-book-panel]]))

(defn- initializing []
  [:div "Loading..."])

(defn app []
  (if (<sub [:core/initialized?])
    [:<>
     [tickers-panel]
     [order-book-panel]]
    [initializing]))
