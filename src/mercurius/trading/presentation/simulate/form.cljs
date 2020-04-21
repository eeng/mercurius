(ns mercurius.trading.presentation.simulate.form
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.views.components :refer [slider button label]]
            [mercurius.trading.presentation.simulate.flow]))

(defn simulate-form []
  (let [{:keys [values loading? running? progress]} (<sub [:trading/simulate-form])]
    (if running?
      [:div.form
       [:progress.progress.m-t-md.is-primary {:value progress :max 100}]
       [:div.field
        [:div.control.is-expanded
         [button
          {:text "Stop Simulation"
           :icon "stop"
           :type "submit"
           :class "is-dark"
           :on-click #(>evt [:trading/stop-simulation])}]]]]
      [:div.form
       [:div.field
        [label "Number of Traders"]
        [slider {:value (:n-traders values)
                 :on-change #(>evt [:trading/simulate-form-changed {:n-traders %}])
                 :min 1
                 :max 500}]]
       [:div.field
        [label "Orders per Trader"]
        [slider {:value (:n-orders-per-trader values)
                 :on-change #(>evt [:trading/simulate-form-changed {:n-orders-per-trader %}])
                 :min 1
                 :max 10}]]
       [:div.field
        [:div.control.is-expanded
         [button
          {:text "Start Simulation"
           :icon "play"
           :type "submit"
           :class ["is-primary" (when loading? "is-loading")]
           :on-click #(>evt [:trading/start-simulation])}]]]])))
