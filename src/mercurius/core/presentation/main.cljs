(ns mercurius.core.presentation.main
  (:require [reagent.dom :as rd]
            [mercurius.core.presentation.app :refer [app]]))

(defn main []
  (rd/render [app]
             (.getElementById js/document "app")))

(main)
