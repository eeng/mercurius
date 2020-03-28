(ns mercurius.trading.adapters.processes.ticker-updater
  (:require [clojure.core.async :refer [go-loop <!]]
            [taoensso.timbre :as log]))

(defn new-ticker-updater [{:keys [subscribe update-ticker]}]
  (log/info "Starting ticker updater")
  (let [events (subscribe :trade-made)]
    (go-loop []
      (if-let [{trade :data} (<! events)]
        (do
          (log/debug "Trade made:" trade)
          (update-ticker trade)
          (recur))
        (log/info "Stopping ticker updater")))))
