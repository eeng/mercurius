(ns mercurius.simulation.adapters.controllers.simulation-controller
  (:require [mercurius.simulation.adapters.processes.simulator :refer [start-simulator stop-simulator]]))

(defn new-simulation-controller [{:keys [simulator]}]
  (fn [[request-type params] _context]
    (case request-type
      :start-simulation (start-simulator simulator params)
      :stop-simulation (stop-simulator simulator))
    [:ok]))
