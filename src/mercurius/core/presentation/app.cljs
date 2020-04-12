(ns mercurius.core.presentation.app
  (:require [mercurius.core.presentation.util.reframe :refer [<sub]]
            [mercurius.core.presentation.flow]
            [mercurius.core.presentation.views.components :refer [page-loader]]
            [mercurius.accounts.presentation.flow]
            [mercurius.accounts.presentation.views.login :refer [login-page]]
            [mercurius.trading.presentation.flow]
            [mercurius.trading.presentation.views.page :refer [trading-page]]))

(defn app []
  (if (<sub [:core/initialized?])
    (if (<sub [:accounts/logged-in?])
      [trading-page]
      [login-page])
    [page-loader]))
