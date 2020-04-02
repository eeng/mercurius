(ns mercurius.core.presentation.main
  (:require [reagent.dom :as rd]
            [mercurius.core.presentation.api :as api]
            [mercurius.core.presentation.app :refer [app]]))

(defn main []
  (api/connect!)
  (rd/render [app]
             (.getElementById js/document "app")))

(main)
