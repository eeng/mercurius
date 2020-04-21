(ns mercurius.trading.presentation.sidebar
  (:require [reagent.core :as r]
            [mercurius.util.collections :refer [index-by]]
            [mercurius.core.presentation.views.components :refer [button panel icon-button]]
            [mercurius.trading.presentation.place-order.form :refer [place-order-form]]
            [mercurius.trading.presentation.deposit.form :refer [deposit-form]]
            [mercurius.trading.presentation.simulate.form :refer [simulate-form]]))

(defonce active-menu (r/atom nil))

(defn- back-button []
  [icon-button {:icon "arrow-left"
                :class "is-small is-outlined is-dark"
                :on-click #(reset! active-menu nil)}])

(defn push-menu [items]
  (if-let [{:keys [activator content]} (-> (index-by :name items)
                                           (get @active-menu))]
    [panel {:header (:text activator)
            :action [back-button]}
     content]
    (into
     [:div.push-menu]
     (for [{:keys [name activator]} items
           :let [set-active #(reset! active-menu name)]]
       [button (assoc activator :on-click set-active)]))))

(defn sidebar []
  [:div.sidebar
   [push-menu
    [{:name "deposit"
      :activator {:text "Deposit" :icon "dollar-sign"}
      :content [deposit-form]}
     {:name "place-order"
      :activator {:text "Place Order" :icon "shopping-cart"}
      :content [place-order-form]}
     {:name "simulate"
      :activator {:text "Simulate" :icon "robot"}
      :content [simulate-form]}]]])
