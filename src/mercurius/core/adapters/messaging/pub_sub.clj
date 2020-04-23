(ns mercurius.core.adapters.messaging.pub-sub
  (:require [clojure.spec.alpha :as s]))

(defprotocol PubSub
  (publish [this topic message]
    "Sends the `message` to the specified `topic` (string).")

  (subscribe [this topic-pattern opts]
    "Allows to be notified when a message is sent to a topic matching `topic-pattern`.
    `opts` should contain an :on-message callback.
    Returns a subscription id that may be used later to unsubscribe.
    Multiple subscribers to the same `topic-pattern` are allowed.")

  (unsubscribe [this subscription]))

(s/fdef publish
  :args (s/cat :pubsub any? :topic string? :message any?))
