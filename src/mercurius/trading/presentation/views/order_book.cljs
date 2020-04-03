(ns mercurius.trading.presentation.views.order-book
  (:require [mercurius.core.presentation.util.reframe :refer [<sub]]))

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

(defn order-book-panel []
  (let [filters (<sub [:trading/order-book-filters])
        {:keys [data loading?]} (when filters
                                  (<sub [:trading/order-book filters]))]
    [:div.panel
     [:div "Order Book"]
     (cond
       (not filters)
       [:div "Select a ticker"]

       loading?
       [:div "Loading..."]

       data
       [:div
        [buying-table (:buying data)]
        [selling-table (:selling data)]])]))
