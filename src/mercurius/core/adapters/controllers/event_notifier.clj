(ns mercurius.core.adapters.controllers.event-notifier
  "Listens to domain events and publishes them to topics so they can be pushed to the interested clients."
  (:require [mercurius.core.domain.messaging.event-bus :refer [listen]]
            [mercurius.core.adapters.messaging.pub-sub :refer [publish]]))

(defn push-topic [& args]
  (apply str "push." args))

(defn start-event-notifier [{:keys [event-bus pub-sub]}]
  ;; Encoding the ticker in the topic allow clients to receive updates only on that ticker.
  (listen event-bus
          :trade-processed
          (fn [{:keys [data]}]
            (let [topic (push-topic "trade-executed." (:ticker data))]
              (publish pub-sub topic data))))

  (listen event-bus
          :ticker-updated
          (fn [{:keys [_ data]}]
            (let [topic (push-topic "ticker-updated." (:ticker data))]
              (publish pub-sub topic data))))

  (listen event-bus
          #{:order-placed :trade-made}
          (fn [{:keys [data]}]
            (let [topic (push-topic "order-book-updated." (:ticker data))]
              (publish pub-sub topic :no-data))))

  (listen event-bus
          #{:deposited-into-wallet :withdrawn-from-wallet :reserved-from-wallet :cancelled-wallet-reserve}
          (fn [{:keys [data]}]
            (let [topic (push-topic "wallet-changed." (:user-id data))]
              (publish pub-sub topic data)))))
