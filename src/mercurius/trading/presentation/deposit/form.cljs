(ns mercurius.trading.presentation.deposit.form
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.views.components :refer [input select button label]]
            [mercurius.trading.presentation.deposit.flux]))

(defn deposit-form []
  (let [{:keys [values loading? valid?]} (<sub [:trading/deposit-form])]
    [:form.form {:on-submit (fn [e]
                              (>evt [:trading/deposit])
                              (.preventDefault e))}
     [:div.field
      [label "Amount"]
      [input {:auto-focus true
              :value (:amount values)
              :on-change #(>evt [:trading/deposit-form-changed {:amount %}])}]]
     [:div.field
      [label "Currency"]
      [select {:collection [["US Dollars" "USD"]
                            ["Bitcoin" "BTC"]
                            ["Ethereum" "ETH"]]
               :value (:currency values)
               :on-change #(>evt [:trading/deposit-form-changed {:currency %}])
               :class "is-fullwidth"}]]
     [:div.field
      [button
       {:text "Confirm"
        :type "submit"
        :disabled (not valid?)
        :class ["is-primary" (when loading? "is-loading")]}]]]))
