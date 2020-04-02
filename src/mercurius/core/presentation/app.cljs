(ns mercurius.core.presentation.app
  (:require [reagent.dom :as rd]
            [taoensso.sente :as sente]))

(defn csrf-token []
  (-> js/document (.querySelector "meta[name='csrf-token']") .-content))

(defn app []
  (let [{:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket! "/chsk" (csrf-token) {:type :auto})]
    ;; (def chsk       chsk)
    ;; (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
    ;; (def chsk-send! send-fn) ; ChannelSocket's send API fn
    ;; (def chsk-state state)   ; Watchable, read-only atom
    )

  [:div "hola"])

(rd/render [app]
           (.getElementById js/document "app"))
