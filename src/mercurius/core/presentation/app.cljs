(ns mercurius.core.presentation.app
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.flow]
            [mercurius.core.presentation.views.components :refer [page-loader]]
            [mercurius.accounts.presentation.flow]
            [mercurius.accounts.presentation.views.login :refer [login-page]]
            [mercurius.trading.presentation.flow]
            [mercurius.trading.presentation.views.page :refer [trading-page]]))

(defn- navbar []
  [:nav.navbar.is-dark
   [:div.navbar-brand
    [:div.navbar-item.is-uppercase.has-text-weight-bold "Mercurius"]]
   [:div.navbar-end
    [:div.navbar-item
     [:a.button.is-black
      {:on-click #(>evt [:logout])}
      [:span.icon [:i.fas.fa-sign-out-alt]]
      [:span "Logout"]]]]])

(defn app []
  (if (<sub [:core/initialized?])
    (if (<sub [:logged-in?])
      [:<>
       [navbar]
       [trading-page]]
      [login-page])
    [page-loader]))
