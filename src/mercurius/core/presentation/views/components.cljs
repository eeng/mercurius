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

(defn label [text]
  [:label.label text])

(defn- value-and-on-change [{:keys [value on-change] :or {on-change identity} :as opts}]
  (assoc opts
         :value (or value "")
         :on-change #(on-change (-> % .-target .-value))))

(defn input [opts]
  [:input.input (value-and-on-change opts)])

(defn select [{:keys [collection] :as opts}]
  (let [html-opts (dissoc opts :collection :on-change :value)]
    [:div.select html-opts
     [:select
      (value-and-on-change opts)
      (for [[text value] collection]
        [:option {:key value :value value} text])]]))

(defn slider [{:keys [value] :as opts}]
  [:<>
   [:input.slider.is-fullwidth.is-circle.has-output
    (merge
     {:step 1 :min 0 :max 100 :type "range"}
     (value-and-on-change opts))]
   [:output value]])

(defn button [{:keys [icon text] :as opts}]
  (let [opts (dissoc opts :icon :text)]
    [:button.button.is-fullwidth opts
     (when icon
       [:span.icon
        [:i.fas {:class (str "fa-" icon)}]])
     [:span text]]))

(defn icon-button [{:keys [icon] :as opts}]
  (let [opts (dissoc opts :icon)]
    [:button.button.is-rounded
     opts
     [:span.icon
      [:i.fas {:class (str "fa-" icon)}]]]))
