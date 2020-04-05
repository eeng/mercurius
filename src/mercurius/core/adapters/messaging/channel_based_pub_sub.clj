(ns mercurius.core.adapters.messaging.channel-based-pub-sub
  (:require [mercurius.core.domain.messaging.pub-sub :refer [PubSub]]
            [clojure.core.async :refer [put! chan mult close! tap go-loop <!]]
            [taoensso.timbre :as log]))

(defrecord ChannelBasedPubSub [in-chan]
  PubSub

  (publish [_ topic message]
    (put! in-chan [topic message]))

  (subscribe [_ topic callback]
    (let [out-chan (chan)]
      (tap (mult in-chan) out-chan)
      (go-loop []
        (when-some [[topic msg] (<! out-chan)]
          (callback msg)
          (recur))))))

(defn start-channel-based-pub-sub []
  (log/info "Starting ChannelBasedPubSub")
  (let [in-chan (chan)]
    (ChannelBasedPubSub. in-chan)))

(defn stop-channel-based-pub-sub [{:keys [in-chan]}]
  (log/info "Stopping channel based pub sub")
  (close! in-chan))
