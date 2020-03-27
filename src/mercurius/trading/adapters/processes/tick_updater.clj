(ns mercurius.trading.adapters.processes.tick-updater
  (:require [clojure.core.async :refer [go-loop <!]]
            [taoensso.timbre :as log]))

(defn new-tick-updater [{:keys [subscribe]}]
  (log/info "Starting tick updater")
  (let [events (subscribe :trading/trade-made)]
    (go-loop []
      (if-let [{trade :data} (<! events)]
        (do
          (println "Received" trade)
          (recur))
        (log/info "Stopping tick updater")))))
