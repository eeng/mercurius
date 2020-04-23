(ns mercurius.core.infraestructure.messaging.channel-based-pub-sub
  (:require [clojure.core.async :refer [put! chan close! go-loop <! sliding-buffer]]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [mercurius.core.adapters.messaging.pub-sub :refer [PubSub]]
            [mercurius.util.uuid :refer [uuid]]))

(defrecord ChannelBasedPubSub [in-chan subscribers]
  PubSub

  (publish [_ topic message]
    (log/debug "Publish" [topic message])
    (put! in-chan [topic message]))

  (subscribe [_ topic-pattern callback]
    (log/debug "Subscribing to" topic-pattern "with" callback)
    (let [handle (uuid)]
      (swap! subscribers assoc handle [topic-pattern callback])
      handle))

  (unsubscribe [_ handle]
    (swap! subscribers dissoc handle)
    :ok))

(defn match-topic? [pattern topic]
  (str/starts-with? topic (str/replace pattern "*" "")))

(defn- start-listener [in-chan subscribers]
  (go-loop []
    (when-some [[topic msg] (<! in-chan)]
      (doseq [[topic-pattern callback] (vals @subscribers)
              :when (match-topic? topic-pattern topic)]
        (try
          (callback msg)
          (catch Exception e
            (log/error "Error processing" {:msg msg :topic-pattern topic-pattern :callback callback})
            (log/error e))))
      (recur))))

(defn start-channel-based-pub-sub []
  (log/info "Starting channel based pubsub")
  (let [in-chan (chan (sliding-buffer 1048576))
        subscribers (atom {})]
    (start-listener in-chan subscribers)
    (ChannelBasedPubSub. in-chan subscribers)))

(defn stop-channel-based-pub-sub [{:keys [in-chan]}]
  (log/info "Stopping channel based pubsub")
  (close! in-chan))
