(ns mercurius.core.presentation.app
  (:require [reagent.dom :as rd]))

(defn app []
  [:div "hola"])

(rd/render [app]
           (.getElementById js/document "app"))
