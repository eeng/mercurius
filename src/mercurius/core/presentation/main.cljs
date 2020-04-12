(ns mercurius.core.presentation.main
  (:require [reagent.dom :as rd]
            [re-frame.core :as re-frame :refer [dispatch-sync]]
            [mercurius.core.presentation.backend :as backend]
            [mercurius.core.presentation.app :refer [app]]))

(defn start []
  (re-frame/clear-subscription-cache!)
  (rd/render [app]
             (.getElementById js/document "app")))

(defn init []
  (println "Starting app...")
  (dispatch-sync [:core/initialize])
  (backend/connect!)
  (start))
