(ns mercurius.core.presentation.app
  (:require [reagent.dom :as rd]
            [mercurius.core.presentation.api :as api]
            [mercurius.trading.presentation.components.tickers :refer [tickers-panel]]))

(defn app []
  [:div
   [tickers-panel]])

(defn main []
  (api/start-remote-events-processor
   (fn []
     (rd/render [app]
                (.getElementById js/document "app")))))

(main)
