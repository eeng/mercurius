(ns mercurius.core.presentation.views.components)

(defn page-loader []
  [:div.page-loader
   [:div.loader]])

(defn loader []
  [:div.loader])

(defn panel [{:keys [header actions loading? class]} & body]
  [:div.panel {:class class}
   [:div.panel-heading
    [:div.level
     [:div.level-left
      [:div.level-item header]]
     (when (seq actions)
       [:div.level-right
        (into [:div.level-item] actions)])]]
   (if loading?
     [:div.panel-block.has-loader [loader]]
     (into [:div.panel-block] body))])
