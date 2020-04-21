(ns mercurius.simulation.adapters.processes.simulator
  (:require [clojure.core.async :refer [thread]]
            [taoensso.timbre :as log]))

(defrecord Simulator [running dispatch])

(defn new-simulator [{:keys [dispatch]}]
  (Simulator. (atom false) dispatch))

(defn start-simulator [{:keys [running]} params]
  (when (compare-and-set! running false true)
    (log/info "Starting simulator")
    (thread
      (while @running
        (println "Running" params)
        (Thread/sleep 1000)))))

(defn stop-simulator [{:keys [running]}]
  (reset! running false)
  (log/info "Stopping simulator"))

(comment
  (def sim (new-simulator {}))
  (start-simulator sim {:n-orders-per-trader 10})
  (stop-simulator sim))
