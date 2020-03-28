(ns mercurius.trading.adapters.processes.ticker-updater
  (:require [clojure.core.async :refer [go-loop <!]]
            [taoensso.timbre :as log]))

(defn new-ticker-updater [{:keys [subscribe update-ticker]}]
  (let [events (subscribe :trade-made)]
    (go-loop []
      (log/info "Starting ticker updater")
      (if-let [{trade :data} (<! events)]
        (do
          (log/debug "Trade made:" trade)
          (update-ticker trade)
          (recur))
        (log/info "Stopping ticker updater")))))
