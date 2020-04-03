(ns mercurius.core.presentation.app
  (:require [mercurius.core.presentation.util :refer [<sub]]
            [mercurius.core.presentation.events]
            [mercurius.trading.presentation.subs]
            [mercurius.trading.presentation.views.tickers :refer [tickers-panel]]))

(defn- initializing []
  [:div "Loading..."])

(defn app []
  (if (<sub [:initialized?])
    [:div
     [tickers-panel]]
    [initializing]))
