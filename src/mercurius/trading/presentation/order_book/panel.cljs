(ns mercurius.trading.presentation.order-book.panel
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.views.components :refer [panel icon-button]]
            [mercurius.trading.presentation.order-book.flow]))

(defn- buying-table [orders]
  [:table.table.is-narrow.is-fullwidth
   [:thead>tr
    [:th.has-text-success {:align "right"} "Count"]
    [:th.has-text-success {:align "right"} "Amount"]
    [:th.has-text-success {:align "right"} "Price"]]
   [:tbody
    (for [{:keys [count amount price]} orders]
      [:tr {:key price}
       [:td {:align "right"} count]
       [:td {:align "right"} amount]
       [:td {:align "right"} price]])]])

(defn- selling-table [orders]
  [:table.table.is-narrow.is-fullwidth
   [:thead>tr
    [:th.has-text-danger "Price"]
    [:th.has-text-danger "Amount"]
    [:th.has-text-danger "Count"]]
   [:tbody
    (for [{:keys [count amount price]} orders]
      [:tr {:key price}
       [:td price]
       [:td amount]
       [:td count]])]])

(defn- decrease-precision-btn []
  [icon-button
   {:icon "minus"
    :class "is-small is-outlined is-dark"
    :on-click #(>evt [:trading/decrease-book-precision])
    :disabled (<sub [:trading/cant-decrease-book-precision])}])

(defn- increase-precision-btn []
  [icon-button
   {:icon "plus"
    :class "is-small is-outlined is-dark"
    :on-click #(>evt [:trading/increase-book-precision])
    :disabled (<sub [:trading/cant-increase-book-precision])}])

(defn order-book-panel []
  (let [{:keys [ticker] :as filters} (<sub [:trading/order-book-filters])
        {:keys [data loading?]} (<sub [:trading/order-book filters])]
    [panel
     {:header "Order Book"
      :subheader ticker
      :action [:<>
               [decrease-precision-btn]
               [increase-precision-btn]]
      :loading? loading?
      :class "order-book clipped"}
     (if (or (seq (:buying data)) (seq (:selling data)))
       [:div.columns
        [:div.column [buying-table (:buying data)]]
        [:div.column [selling-table (:selling data)]]]
       [:div "Nothing to show here."])]))
