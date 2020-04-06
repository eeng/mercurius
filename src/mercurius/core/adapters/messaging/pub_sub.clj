(ns mercurius.core.adapters.messaging.pub-sub)

(defprotocol PubSub
  (publish [this topic message]
    "Sends the `message` to the specified `topic`.")

  (subscribe [this topic callback]
    "When a message is sent to the topic, the callback will be called with the meesage."))
