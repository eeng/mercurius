(ns mercurius.core.presentation.main
  (:require [reagent.dom :as rd]
            [re-frame.core :as re-frame :refer [dispatch-sync]]
            [mercurius.core.presentation.util.reframe :refer [>evt]]
            [mercurius.core.presentation.socket :as socket]
            [mercurius.core.presentation.app :refer [app]]))

(defn start []
  (re-frame/clear-subscription-cache!)
  (rd/render [app]
             (.getElementById js/document "app")))

(defn init []
  (println "Starting app...")
  (dispatch-sync [:core/initialize])
  (socket/connect! :on-connect #(>evt [:core/socket-connected (:uid %)]))
  (start))
