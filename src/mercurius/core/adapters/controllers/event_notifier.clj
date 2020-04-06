(ns mercurius.core.adapters.controllers.event-notifier
  "Listens to domain events and publishes them to topics so they can be pushed to the interested clients."
  (:require [mercurius.core.domain.messaging.event-bus :refer [listen]]
            [mercurius.core.adapters.messaging.pub-sub :refer [publish]]))

(defn start-event-notifier [{:keys [event-bus pub-sub]}]
  (listen event-bus
          :ticker-updated
          (fn [{:keys [type data]}]
            ;; Encoding the ticker in the topic would allow clients
            ;; to receive updates only on that ticker.
            (let [topic (str "push.ticker-updated." (:ticker data))]
              (publish pub-sub topic [type data])))))
