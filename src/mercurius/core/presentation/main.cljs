(ns mercurius.core.presentation.app
  (:require [reagent.dom :as rd]
            [mercurius.core.presentation.components.tickers :refer [tickers-panel]]))

(defn app []
  [:div
   [tickers-panel]])

(defn main []
  (rd/render [app]
             (.getElementById js/document "app")))

(main)
