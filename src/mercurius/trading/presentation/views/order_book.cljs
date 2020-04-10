(ns mercurius.trading.presentation.views.order-book
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.views.components :refer [panel]]))

(defn- buying-table [orders]
  [:div {:style {:display "inline-block"}}
   [:div "Buying"]
   [:table
    [:thead>tr
     [:th "Count"]
     [:th "Amount"]
     [:th "Price"]]
    [:tbody
     (for [{:keys [count amount price]} orders]
       [:tr {:key price}
        [:td count]
        [:td amount]
        [:td price]])]]])

(defn- selling-table [orders]
  [:div {:style {:display "inline-block"}}
   [:div "Selling"]
   [:table
    [:thead>tr
     [:th "Price"]
     [:th "Amount"]
     [:th "Count"]]
    [:tbody
     (for [{:keys [count amount price]} orders]
       [:tr {:key price}
        [:td price]
        [:td amount]
        [:td count]])]]])

(defn- increase-precision-btn []
  [:button {:on-click #(>evt [:trading/increase-book-precision])
            :disabled (<sub [:trading/cant-increase-book-precision])}
   "+"])

(defn- decrease-precision-btn []
  [:button {:on-click #(>evt [:trading/decrease-book-precision])
            :disabled (<sub [:trading/cant-decrease-book-precision])}
   "-"])

(defn order-book-panel []
  (let [{:keys [ticker] :as filters} (<sub [:trading/order-book-filters])]
    (when ticker
      (let [{:keys [data loading?]} (<sub [:trading/order-book filters])]
        [panel
         {:header (str "Order Book for " ticker)
          :actions [[increase-precision-btn]
                    [decrease-precision-btn]]
          :loading? loading?}
         (when data
           [:div
            [buying-table (:buying data)]
            [selling-table (:selling data)]])]))))
