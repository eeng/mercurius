(ns mercurius.core.adapters.web.index
  (:require [hiccup.page :as page]
            [mercurius.core.adapters.web.helpers :refer [ok]]))

(defn index [{:keys [anti-forgery-token]}]
  (ok
   (page/html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
     [:meta {:name "csrf-token" :content anti-forgery-token}]
     [:title "Mercurius"]
     [:body
      [:div#app]
      [:script {:src "js/main.js"}]]])))
