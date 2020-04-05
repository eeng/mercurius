(ns mercurius.core.adapters.processes.activity-logger
  (:require [mercurius.core.domain.messaging.event-bus :refer [listen]]
            [taoensso.timbre :as log]))

(defn new-activity-logger
  "Starts a background job that logs every time a ticker price changes."
  [{:keys [event-bus]}]
  (log/info "Starting activity logger")
  (listen event-bus
          :ticker-updated
          (fn [{:keys [type data]}]
            (log/info "New event:" [type data]))))
