(ns mercurius.accounts.presentation.login.page
  (:require [mercurius.core.presentation.util.reframe :refer [>evt <sub]]
            [mercurius.core.presentation.views.components :refer [input]]
            [mercurius.accounts.presentation.login.flow]))

(defn- login-form []
  (let [{:keys [values loading?]} (<sub [:login-form])]
    [:form
     {:on-submit (fn [ev] (>evt [:login]) (.preventDefault ev))}
     [:div.field
      [:div.control.has-icons-left
       [input
        {:placeholder "Username"
         :value (:username values)
         :on-change #(>evt [:login-form-changed {:username %}])
         :auto-focus true}]
       [:span.icon.is-small.is-left
        [:i.fas.fa-user]]]]
     [:div.field
      [:div.control.has-icons-left
       [input
        {:type "password"
         :placeholder "Password"
         :value (:password values)
         :on-change #(>evt [:login-form-changed {:password %}])}]
       [:span.icon.is-small.is-left
        [:i.fas.fa-lock]]]]
     [:div.field
      [:button.button.is-fullwidth.is-primary
       {:type "submit"
        :class (when loading? "is-loading")}
       "ENTER"]]]))

(defn login-page []
  [:section.hero.is-dark.is-bold.is-fullheight
   [:div.hero-head
    [:nav.navbar
     [:div.navbar-brand
      [:div.navbar-item.is-uppercase.has-text-weight-bold "Mercurius"]]]]
   [:div.hero-body.has-text-centered
    [:div.container
     [:h1.title "Sign In"]
     [:h2.subtitle "Enter your credentials"]
     [:div.columns.is-centered
      [:div.column.is-4.has-text-dark
       [login-form]]]]]
   [:div.hero-foot
    [:div.navbar
     [:div.navbar-end
      [:div.navbar-item "Developed by "
       [:a.m-l-xs {:href "https://github.com/eeng"} "eeng"]]]]]])
