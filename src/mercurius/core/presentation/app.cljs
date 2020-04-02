(ns mercurius.core.presentation.app
  (:require [reagent.dom :as rd]
            [taoensso.sente :as sente]))

(defn csrf-token []
  (-> js/document (.querySelector "meta[name='csrf-token']") .-content))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" (csrf-token) {:type :auto})]
  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )

(defn app []
  (js/setTimeout #(chsk-send! [:backend/request [:get-tickers]]
                              5000
                              (fn [reply]
                                (when (sente/cb-success? reply)
                                  (println reply))))
                 1000)
  (js/setTimeout #(chsk-send! [:backend/request [:get-order-book {:ticker "BTCUSD" :precision "P1" :limit 10}]]) 1500)
  [:div "hola"])

(defn init []
  (rd/render [app]
             (.getElementById js/document "app")))

(init)
