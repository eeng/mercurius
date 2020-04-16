(ns mercurius.trading.adapters.processes.trade-processor
  (:require [taoensso.timbre :as log]
            [mercurius.core.domain.messaging.event-bus :refer [listen]]
            [mercurius.trading.domain.repositories.trade-repository :refer [adapt-for-storage]]))

(defn new-trade-processor [{:keys [event-bus process-trade]}]
  (log/info "Starting trade processor")
  (listen event-bus
          :trade-made
          (fn [{trade :data :as event}]
            (process-trade (adapt-for-storage trade event)))))
