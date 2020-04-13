(ns mercurius.core.infraestructure.web.index
  (:require [hiccup.page :as page]
            [mercurius.util.ring :refer [ok]]))

(defn index [{:keys [anti-forgery-token]}]
  (ok
   (page/html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
     [:meta {:name "csrf-token" :content anti-forgery-token}]
     [:title "Mercurius"]
     [:link {:rel "stylesheet" :href "/css/main.css"}]
     [:script {:src "https://kit.fontawesome.com/2b86c42320.js" :crossorigin "anonymous"}]]
    [:body
     [:div#app]
     [:script {:src "js/main.js"}]])))
