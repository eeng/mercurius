(ns mercurius.trading.presentation.place-order.form
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.views.components :refer [input select]]
            [mercurius.trading.presentation.place-order.flow]))

(defn place-order-form []
  (let [[amount-cur price-cur] (<sub [:trading/ticker-selected-currencies])
        {:keys [values loading? valid?]} (<sub [:trading/place-order-form])
        place-order (fn [side] (>evt [:trading/place-order side]))]
    [:div.form
     [:div.field
      [:label.label "Order Type"]
      [:div.control.is-expanded
       [select {:collection [["Limit" "limit"] ["Market" "market"]]
                :value (:type values)
                :on-change #(>evt [:trading/place-order-form-changed {:type %}])
                :class "is-fullwidth"
                :auto-focus true}]]]
     [:div.field
      [:label.label "Amount"]
      [input {:placeholder amount-cur
              :value (:amount values)
              :on-change #(>evt [:trading/place-order-form-changed {:amount %}])}]]
     (when (= "limit" (:type values))
       [:div.field
        [:label.label "Price"]
        [input {:placeholder price-cur
                :value (:price values)
                :on-change #(>evt [:trading/place-order-form-changed {:price %}])}]])
     [:div.field.is-grouped
      [:div.control.is-expanded
       [:button.button.is-success.is-fullwidth
        {:disabled (not valid?)
         :class (when loading? "is-loading")
         :on-click #(place-order :buy)}
        "Buy"]]
      [:div.control.is-expanded
       [:button.button.is-danger.is-fullwidth
        {:disabled (not valid?)
         :class (when loading? "is-loading")
         :on-click #(place-order :sell)}
        "Sell"]]]]))
