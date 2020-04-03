(ns mercurius.trading.presentation.views.tickers
  (:require [mercurius.core.presentation.util :refer [<sub]]))

(defn tickers-panel []
  (let [tickers (<sub [:trading/tickers])]
    [:div "Tickers"
     (pr-str tickers)]))
