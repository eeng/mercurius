(ns mercurius.core.adapters.messaging.pub-sub
  (:require [clojure.spec.alpha :as s]))

(defprotocol PubSub
  (publish [this topic message]
    "Sends the `message` to the specified `topic` (string).")

  (subscribe [this topic callback]
    "When a message is sent to the topic, the callback will be called with the meesage.
    It should return some kind of handle that may be used to unsubscribe")

  (unsubscribe [this handle]))

(s/fdef publish
  :args (s/cat :pubsub any? :topic string? :message any?))
