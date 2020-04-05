(ns mercurius.core.adapters.controllers.pub-sub)

(defprotocol PubSub
  "Allows bidirectional communication with the Web clients.
  Messages should be in the form [msg-type msg-data]."

  (subscribe [this msg-type callback]
    "Listens to events sent by the clients.")

  (broadcast! [this msg]
    "Sends the message to all connected users."))
