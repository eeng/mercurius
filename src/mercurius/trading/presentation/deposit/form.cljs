(ns mercurius.trading.presentation.deposit.form
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.views.components :refer [input select]]
            [mercurius.trading.presentation.deposit.flow]))

(defn deposit-form []
  (let [{:keys [values loading? valid?]} (<sub [:trading/deposit-form])]
    [:form.form {:on-submit (fn [e]
                              (>evt [:trading/deposit])
                              (.preventDefault e))}
     [:div.field
      [:label.label "Amount"]
      [input {:auto-focus true
              :value (:amount values)
              :on-change #(>evt [:trading/deposit-form-changed {:amount %}])}]]
     [:div.field
      [:label.label "Currency"]
      [:div.control.is-expanded
       [select {:collection [["US Dollars" "USD"]
                             ["Bitcoin" "BTC"]
                             ["Ethereum" "ETH"]]
                :value (:currency values)
                :on-change #(>evt [:trading/deposit-form-changed {:currency %}])
                :class "is-fullwidth"}]]]
     [:div.field
      [:div.control.is-expanded
       [:button.button.is-primary.is-fullwidth
        {:type "submit"
         :disabled (not valid?)
         :class (when loading? "is-loading")}
        "Confirm"]]]]))
