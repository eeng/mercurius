(ns mercurius.trading.presentation.sidebar
  (:require [reagent.core :as r]
            [mercurius.core.presentation.views.components :refer [button]]
            [mercurius.trading.presentation.place-order.form :refer [place-order-form]]))

(defn- accordion-item [{:keys [activator content]} active? set-active]
  [:div.accordion-item
   [:div.accordion-activator
    [button (assoc activator
                   :class (when active? "is-dark")
                   :on-click set-active)]]
   (when active?
     [:div.accordion-content
      content])])

(defn accordion [{:keys [items initial]}]
  (let [active (r/atom initial)]
    (fn []
      (into
       [:div.accordion]
       (for [{:keys [name] :as item} items
             :let [active? (= @active name)
                   set-active #(reset! active name)]]
         [accordion-item item active? set-active])))))

(defn sidebar []
  [:div.sidebar
   [accordion
    {:items
     [{:name "deposit"
       :activator {:text "Deposit" :icon "dollar-sign"}
       :content [:div "the content"]}
      {:name "place-order"
       :activator {:text "Place Order" :icon "shopping-cart"}
       :content [place-order-form]}
      {:name "simulate"
       :activator {:text "Simulator" :icon "robot"}
       :content [:div "the content"]}]
     :initial "deposit"}]])
