(ns mercurius.trading.presentation.simulate.form
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.views.components :refer [input select button]]
            [mercurius.trading.presentation.simulate.flow]))

(defn simulate-form []
  (let [{:keys [values loading? running?]} (<sub [:trading/simulate-form])]
    [:div.form
     [:div.field
      [:div.control.is-expanded
       (if running?
         [button
          {:text "Stop Simulation"
           :icon "stop"
           :type "submit"
           :class "is-light"
           :on-click #(>evt [:trading/stop-simulation])}]
         [button
          {:text "Start Simulation"
           :icon "play"
           :type "submit"
           :class ["is-primary" (when loading? "is-loading")]
           :on-click #(>evt [:trading/start-simulation])}])]]]))
