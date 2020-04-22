(ns mercurius.core.adapters.processes.activity-logger
  (:require [mercurius.core.domain.messaging.event-bus :refer [listen]]
            [taoensso.timbre :as log]))

(defn new-activity-logger
  "Starts a background job that logs every event in the system."
  [{:keys [event-bus]}]
  (log/info "Starting activity logger")
  (listen event-bus
          (constantly true)
          (fn [{:keys [type data]}]
            (log/debug "New event" [type data]))))
