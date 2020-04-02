(ns mercurius.core.presentation.components.tickers
  (:require [mercurius.core.presentation.api :as api]))

(defn tickers-panel []
  (let [response (api/use-query [:get-tickers])]
    (fn []
      [:div "Tickers"
       (pr-str @response)])))
