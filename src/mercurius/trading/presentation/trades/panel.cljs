(ns mercurius.trading.presentation.trades.panel
  (:require [mercurius.core.presentation.util.reframe :refer [<sub]]
            [mercurius.core.presentation.util.format :refer [format-time]]
            [mercurius.core.presentation.views.components :refer [panel]]
            [mercurius.trading.presentation.trades.flow]))

(defn- direction-icon [direction]
  (case direction
    :up [:span.icon.is-small.has-text-success
         [:i.fas.fa-chevron-up]]
    :down [:span.icon.is-small.has-text-danger
           [:i.fas.fa-chevron-down]]))

(defn trades-panel []
  (let [ticker (<sub [:trading/ticker-selected])
        {:keys [data loading?]} (<sub [:trading/trades ticker])]
    [panel {:header "Trades" :subheader ticker :loading? loading? :class "clipped trades"}
     (if (seq data)
       (for [{:keys [id price amount created-at direction]} data]
         [:div.item {:key id}
          [:div.direction [direction-icon direction]]
          [:div.price.has-text-weight-bold.is-size-5
           {:class (case direction :up "has-text-success" :down "has-text-danger")}
           price]
          [:div.details
           [:div amount]
           [:div.has-text-grey-light.is-size-7 (format-time created-at)]]])
       [:div "Nothing to show here."])]))
