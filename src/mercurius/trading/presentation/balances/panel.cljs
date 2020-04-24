(ns mercurius.trading.presentation.balances.panel
  (:require [mercurius.core.presentation.util.reframe :refer [<sub]]
            [mercurius.core.presentation.views.components :refer [panel]]
            [mercurius.trading.presentation.balances.flow]))

(defn balances-panel []
  (let [{:keys [loading? data]} (<sub [:trading/wallets])]
    [panel {:header "Balances" :loading? loading?}
     (if (seq data)
       [:div.level.balances
        (for [{:keys [currency balance reserved]} data]
          [:div.level-item.has-text-centered {:key currency}
           [:div
            [:p.heading.is-size-6 currency]
            [:p.title balance]
            (when (pos? reserved)
              [:p.has-text-grey-light reserved " reserved"])]])]
       [:div "No wallets created yet."])]))
