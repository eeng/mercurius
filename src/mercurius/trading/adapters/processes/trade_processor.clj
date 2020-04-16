(ns mercurius.trading.adapters.processes.trade-processor
  (:require [taoensso.timbre :as log]
            [mercurius.core.domain.messaging.event-bus :refer [listen]]))

(defn new-trade-processor [{:keys [event-bus process-trade]}]
  (log/info "Starting trade processor")
  (listen event-bus
          :trade-made
          (fn [{trade :data :keys [id created-at]}]
            (process-trade (assoc trade :id id :created-at created-at)))))
