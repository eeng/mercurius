(ns mercurius.trading.presentation.views.order-book
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.views.components :refer [panel]]))

(defn- buying-table [orders]
  [:table.table.is-narrow.is-fullwidth
   [:thead>tr
    [:th {:align "right"} "Count"]
    [:th {:align "right"} "Amount"]
    [:th {:align "right"} "Price"]]
   [:tbody
    (for [{:keys [count amount price]} orders]
      [:tr {:key price}
       [:td {:align "right"} count]
       [:td {:align "right"} amount]
       [:td {:align "right"} price]])]])

(defn- selling-table [orders]
  [:table.table.is-narrow.is-fullwidth
   [:thead>tr
    [:th "Price"]
    [:th "Amount"]
    [:th "Count"]]
   [:tbody
    (for [{:keys [count amount price]} orders]
      [:tr {:key price}
       [:td price]
       [:td amount]
       [:td count]])]])

(defn- decrease-precision-btn []
  [:button.button.is-small
   {:on-click #(>evt [:trading/decrease-book-precision])
    :disabled (<sub [:trading/cant-decrease-book-precision])}
   [:span.icon.is-small
    [:i.fas.fa-minus]]])

(defn- increase-precision-btn []
  [:button.button.is-small
   {:on-click #(>evt [:trading/increase-book-precision])
    :disabled (<sub [:trading/cant-increase-book-precision])}
   [:span.icon.is-small
    [:i.fas.fa-plus]]])

(defn order-book-panel []
  (let [{:keys [ticker] :as filters} (<sub [:trading/order-book-filters])]
    (when ticker
      (let [{:keys [data loading?]} (<sub [:trading/order-book filters])]
        [panel
         {:header (str "Order Book for " ticker)
          :actions [[decrease-precision-btn]
                    [increase-precision-btn]]
          :loading? loading?
          :class "order-book"}
         (if (or (seq (:buying data)) (seq (:selling data)))
           [:div.level
            [:div.level-left [buying-table (:buying data)]]
            [:div.level-right [selling-table (:selling data)]]]
           [:div "Nothing to show here."])]))))
