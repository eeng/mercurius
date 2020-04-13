(ns mercurius.trading.adapters.processes.ticker-updater
  (:require [taoensso.timbre :as log]
            [mercurius.core.domain.messaging.event-bus :refer [listen]]
            [mercurius.trading.domain.repositories.trade-repository :refer [adapt-for-storage]]))

(defn new-ticker-updater [{:keys [event-bus process-trade]}]
  (log/info "Starting ticker updater")
  (listen event-bus
          :trade-made
          (fn [{trade :data :as event}]
            (process-trade (adapt-for-storage trade event)))))
