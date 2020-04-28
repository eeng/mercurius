(ns mercurius.core.presentation.app
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.flux]
            [mercurius.core.presentation.views.components :refer [page-loader]]
            [mercurius.accounts.presentation.login.page :refer [login-page]]
            [mercurius.trading.presentation.page :refer [trading-page]]))

(defn- navbar []
  [:nav.navbar.is-dark
   [:div.navbar-brand
    [:div.navbar-item.is-uppercase.has-text-weight-bold "Mercurius"]]
   [:div.navbar-menu
    [:div.navbar-end
     [:div.navbar-item
      [:a.button.is-black
       {:on-click #(>evt [:logout])}
       [:span.icon [:i.fas.fa-sign-out-alt]]
       [:span "Logout"]]]]]])

(defn app []
  (if (<sub [:core/initialized?])
    (if (<sub [:logged-in?])
      [:<>
       [navbar]
       [trading-page]]
      [login-page])
    [page-loader]))
