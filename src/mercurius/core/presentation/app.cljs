(ns mercurius.core.presentation.app
  (:require [mercurius.core.presentation.util.reframe :refer [<sub]]
            [mercurius.core.presentation.flow]
            [mercurius.trading.presentation.flow]
            [mercurius.trading.presentation.views.page :refer [trading-page]]))

(defn- initializing []
  [:div "Loading..."])

(defn app []
  (if (<sub [:core/initialized?])
    [trading-page]
    [initializing]))
