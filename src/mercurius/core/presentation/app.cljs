(ns mercurius.core.presentation.app
  (:require [mercurius.core.presentation.util.reframe :refer [<sub]]
            [mercurius.core.presentation.flow]
            [mercurius.core.presentation.views.components :refer [page-loader]]
            [mercurius.trading.presentation.flow]
            [mercurius.trading.presentation.views.page :refer [trading-page]]))

(defn app []
  (if (<sub [:core/initialized?])
    [trading-page]
    [page-loader]))
