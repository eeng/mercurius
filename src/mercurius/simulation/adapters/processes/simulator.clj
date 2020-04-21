(ns mercurius.simulation.adapters.processes.simulator
  (:require [clojure.core.async :refer [thread]]
            [taoensso.timbre :as log]
            [mercurius.util.progress :refer [new-progress-tracker]]
            [mercurius.core.adapters.messaging.pub-sub :refer [publish]]))

(defrecord Simulator [running dispatch pub-sub])

(defn new-simulator [{:keys [dispatch pub-sub]}]
  (Simulator. (atom false) dispatch pub-sub))

(defn- run-simulation [params {:keys [running progress!]}]
  (loop [i 0]
    (when (and @running (< i 10))
      (println "Running" i params)
      (progress!)
      (Thread/sleep 1000)
      (recur (inc i)))))

;; TODO this fake event [:simulation-progress %] its only needed because the client can't join topics yet
(defn- notify-progress [pub-sub progress]
  (publish pub-sub "push.simulation-progress" [:simulation-progress progress]))

(defn start-simulator [{:keys [running pub-sub]} params]
  (when (compare-and-set! running false true)
    (log/info "Starting simulation with params" params)
    (thread
      (let [{:keys [progress!]} (new-progress-tracker {:total 10
                                                       :on-progress (partial notify-progress pub-sub)})]
        (run-simulation params {:running running :progress! progress!})
        (reset! running false)))))

(defn stop-simulator [{:keys [running pub-sub]}]
  (reset! running false)
  (notify-progress pub-sub 1.0) ; Notify one more time just in case the client receives a progress notification after stopping the simulator
  (log/info "Stopping simulation"))

(comment
  (def sim (new-simulator {}))
  (start-simulator sim {:n-orders-per-trader 10})
  (stop-simulator sim))
