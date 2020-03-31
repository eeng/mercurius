(ns mercurius.trading.adapters.processes.ticker-updater
  (:require [taoensso.timbre :as log]
            [mercurius.core.domain.messaging.event-bus :refer [subscribe-to]]))

(defn new-ticker-updater [{:keys [event-bus update-ticker]}]
  (log/info "Starting ticker updater")
  (subscribe-to event-bus
                :trade-made
                :on-event (fn [{trade :data}]
                            (update-ticker trade))
                :on-close (fn []
                            (log/info "Stopping ticker updater"))))
