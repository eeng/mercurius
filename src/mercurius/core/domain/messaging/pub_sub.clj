(ns mercurius.core.domain.messaging.pub-sub)

(defprotocol PubSub
  (publish [this topic message]
    "Sends the `message` to the specified `topic`.")

  (subscribe [this topic callback]
    "When a message is send to the `topic`, it calls the callback with the message."))
