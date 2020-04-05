(ns mercurius.trading.adapters.processes.ticker-updater
  (:require [taoensso.timbre :as log]
            [mercurius.core.domain.messaging.event-bus :refer [listen]]))

(defn new-ticker-updater [{:keys [event-bus update-ticker]}]
  (log/info "Starting ticker updater")
  (listen event-bus
          :trade-made
          (fn [{trade :data}]
            (update-ticker trade))))
