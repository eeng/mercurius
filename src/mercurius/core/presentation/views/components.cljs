(ns mercurius.core.presentation.views.components)

(defn page-loader []
  [:div.page-loader
   [:div.loader]])

(defn loader []
  [:div.loader])

(defn panel [{:keys [header subheader action loading? class]} & body]
  [:div.panel {:class class}
   [:div.panel-heading
    header
    (when subheader
      [:span.is-size-6.has-text-grey.m-l-sm subheader])
    (when action
      [:div.is-pulled-right action])]
   (if loading?
     [:div.panel-block.has-loader [loader]]
     (into [:div.panel-block] body))])

(defn input [{:keys [value on-change] :or {on-change identity} :as opts}]
  [:input.input
   (assoc opts
          :value (or value "")
          :on-change #(on-change (-> % .-target .-value)))])

(defn select [{:keys [collection value on-change] :or {on-change identity} :as opts}]
  (let [html-opts (dissoc opts :collection :on-change :value)]
    [:div.select html-opts
     [:select
      (assoc opts
             :value (or value "")
             :on-change #(on-change (-> % .-target .-value)))
      (for [[text value] collection]
        [:option {:key value :value value} text])]]))

(defn button [{:keys [icon text] :as opts}]
  (let [opts (dissoc opts :icon :text)]
    [:button.button.is-fullwidth opts
     [:span.icon
      [:i.fas {:class (str "fa-" icon)}]]
     [:span text]]))

(defn icon-button [{:keys [icon] :as opts}]
  (let [opts (dissoc opts :icon)]
    [:button.button.is-rounded
     opts
     [:span.icon
      [:i.fas {:class (str "fa-" icon)}]]]))
