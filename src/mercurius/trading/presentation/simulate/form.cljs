(ns mercurius.trading.presentation.simulate.form
  (:require [mercurius.core.presentation.util.reframe :refer [<sub >evt]]
            [mercurius.core.presentation.views.components :refer [slider button label select]]
            [mercurius.trading.presentation.simulate.flux]))

(defn- start-simulation-controls [{:keys [loading?]}]
  [:div.field
   [:div.control.is-expanded
    [button
     {:text "Start Simulation"
      :icon "play"
      :type "submit"
      :class ["is-primary" (when loading? "is-loading")]
      :on-click #(>evt [:trading/start-simulation])}]]])

(defn- stop-simulation-controls [{:keys [progress loading?]}]
  [:div
   [:p "Running simulation. Please wait..."]
   [:progress.progress.m-t-md.is-primary {:value progress :max 100}]
   [:div.field
    [:div.control.is-expanded
     [button
      {:text "Stop Simulation"
       :icon "stop"
       :type "submit"
       :class ["is-dark" (when loading? "is-loading")]
       :on-click #(>evt [:trading/stop-simulation])}]]]])

(defn simulate-form []
  (>evt [:trading/subscribe-to-simulation-progress])
  (fn []
    (let [{:keys [running? values] :as form} (<sub [:trading/simulate-form])
          {:keys [data] :as tickers} (<sub [:trading/tickers])]
      [:div.form
       [:div {:class (when running? "element is-disabled")}
        [:div.field
         [label "Ticker"]
         [select {:collection (map (juxt :ticker :ticker) (vals data))
                  :value (:ticker values)
                  :on-change #(>evt [:trading/simulate-form-changed {:ticker %}])
                  :class ["is-fullwidth" (when (:loading? tickers) "is-loading")]}]]
        [:div.field
         [label "Initial Price"]
         [slider {:value (:initial-price values)
                  :on-change #(>evt [:trading/simulate-form-changed {:initial-price %}])
                  :min 100
                  :step 100
                  :max 10000}]]
        [:div.field
         [label "Number of traders"]
         [slider {:value (:n-traders values)
                  :on-change #(>evt [:trading/simulate-form-changed {:n-traders %}])
                  :min 1
                  :max 200}]]
        [:div.field
         [label "Orders per trader"]
         [slider {:value (:n-orders-per-trader values)
                  :on-change #(>evt [:trading/simulate-form-changed {:n-orders-per-trader %}])
                  :min 1
                  :max 10}]]
        [:div.field
         [label "Max ms between orders"]
         [slider {:value (:max-ms-between-orders values)
                  :on-change #(>evt [:trading/simulate-form-changed {:max-ms-between-orders %}])
                  :min 100
                  :step 100
                  :max 3000}]]]
       (if running?
         [stop-simulation-controls form]
         [start-simulation-controls form])])))
