(ns mercurius.core.presentation.views.components)

(defn panel [{:keys [header actions loading?]} & body]
  [:div.panel
   [:div.panel-header
    [:div.panel-left header]
    (when (seq actions)
      (into [:div.panel-right] actions))]
   (into [:div.panel-content]
         (if loading?
           [:div "Loading..."]
           body))])
