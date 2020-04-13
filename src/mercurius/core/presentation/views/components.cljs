(ns mercurius.core.presentation.views.components)

(defn page-loader []
  [:div.page-loader
   [:div.loader]])

(defn loader []
  [:div.loader])

(defn panel [{:keys [header subheader actions loading? class]} & body]
  [:div.panel {:class class}
   [:div.panel-heading
    [:div.level
     [:div.level-left
      [:div.level-item
       header
       (when subheader
         [:span.is-size-6.has-text-grey.m-l-sm subheader])]]
     (when (seq actions)
       [:div.level-right
        (into [:div.level-item] actions)])]]
   (if loading?
     [:div.panel-block.has-loader [loader]]
     (into [:div.panel-block] body))])

(defn input [form-atom form-key opts]
  [:input.input
   (merge
    {:value (get @form-atom form-key "")
     :on-change #(swap! form-atom assoc form-key (-> % .-target .-value))}
    opts)])

(defn select [form-atom form-key collection {:keys [parser] :or {parser identity} :as opts}]
  (let [opts (dissoc opts :parser)]
    [:div.select opts
     [:select
      {:value (get @form-atom form-key "")
       :on-change #(swap! form-atom assoc form-key (parser (-> % .-target .-value)))}
      (map (fn [[text value]]
             [:option {:key value :value value} text])
           collection)]]))
