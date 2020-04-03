(ns mercurius.core.presentation.main
  (:require [reagent.dom :as rd]
            [mercurius.core.presentation.api :as api]
            [mercurius.core.presentation.app :refer [app]]))

(defn start []
  (rd/render [app]
             (.getElementById js/document "app")))

(defn init []
  (println "Starting app...")
  (api/connect!)
  (start))
