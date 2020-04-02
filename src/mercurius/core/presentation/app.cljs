(ns mercurius.core.presentation.app
  (:require [mercurius.core.presentation.api :as api]
            [mercurius.trading.presentation.components.tickers :refer [tickers-panel]]))

(defn app []
  (if (api/connected?)
    [:div
     [tickers-panel]]
    [:div "Oops, WebSocket disconnected!"]))
