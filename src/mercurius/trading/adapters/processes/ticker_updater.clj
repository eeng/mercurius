(ns mercurius.trading.adapters.processes.ticker-updater
  (:require [taoensso.timbre :as log]))

(defn new-ticker-updater [{:keys [subscribe-to update-ticker]}]
  (log/info "Starting ticker updater")
  (subscribe-to :trade-made
                :on-event (fn [{trade :data}]
                            (log/debug "Trade made:" trade)
                            (update-ticker trade))))
