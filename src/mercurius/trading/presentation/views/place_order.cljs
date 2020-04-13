(ns mercurius.trading.presentation.views.place-order
  (:require [reagent.core :as r]
            [cljs.core.match :refer-macros [match]]
            [mercurius.core.presentation.util.string :refer [parse-float]]
            [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.views.components :refer [panel input select]]))

(defn- dissoc-price-if-market-order [{:keys [type] :as order}]
  (cond-> order
    (= type :market) (dissoc :price)))

(defn- coerce [order]
  (-> order
      (update :type keyword)
      (update :amount parse-float)
      (update :price parse-float)
      (dissoc-price-if-market-order)))

(defn- validate [order]
  (match [order]
    [{:type :market :amount amount}] (pos? amount)
    [{:type :limit :amount amount :price price}] (and (pos? amount) (pos? price))
    :else false))

(defn place-order-panel []
  (let [order (r/atom {:type :limit})]
    (fn []
      (let [ticker (<sub [:trading/ticker-selected])
            [amount-cur price-cur] (<sub [:trading/ticker-selected-currencies])
            coerced-order (coerce @order)
            valid? (validate coerced-order)
            place-order (fn [side]
                          (>evt [:trading/place-order (assoc coerced-order :side side :ticker ticker)])
                          (reset! order (select-keys coerced-order [:type])))]
        [panel {:header "Place Order"}
         (when ticker
           [:div
            [:div.field
             [:label.label "Order Type"]
             [:div.control.is-expanded
              [select order :type [["Limit" "limit"] ["Market" "market"]]
               {:class "is-fullwidth"}]]]
            [:div.field
             [:label.label "Amount"]
             [input order :amount {:placeholder amount-cur}]]
            (when (= :limit (:type coerced-order))
              [:div.field
               [:label.label "Price"]
               [input order :price {:placeholder price-cur}]])
            [:div.field.is-grouped
             [:div.control.is-expanded
              [:button.button.is-success.is-fullwidth
               {:disabled (not valid?)
                :on-click #(place-order :buy)}
               "Buy"]]
             [:div.control.is-expanded
              [:button.button.is-danger.is-fullwidth
               {:disabled (not valid?)
                :on-click #(place-order :sell)}
               "Sell"]]]])]))))
