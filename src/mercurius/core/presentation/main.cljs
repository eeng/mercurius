(ns mercurius.core.presentation.main
  (:require [reagent.dom :as rd]
            [re-frame.core :as re-frame]
            [mercurius.core.presentation.api :as api]
            [mercurius.core.presentation.app :refer [app]]))

(defn start []
  (re-frame/clear-subscription-cache!)
  (rd/render [app]
             (.getElementById js/document "app")))

(defn init []
  (println "Starting app...")
  (api/connect!)
  (start))
