(ns mercurius.core.adapters.processes.activity-logger
  (:require [mercurius.core.domain.messaging.event-bus :refer [subscribe-to]]
            [taoensso.timbre :as log]))

(defn new-activity-logger
  "Starts a background job that logs every time a ticker price changes."
  [{:keys [event-bus]}]
  (log/info "Starting activity logger")
  (subscribe-to event-bus
                :ticker-updated
                :on-event (fn [{:keys [type data]}]
                            (log/info "New event:" [type data]))))
