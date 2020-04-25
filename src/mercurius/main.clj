(ns mercurius.main
  (:require [mercurius.core.configuration.system :refer [start stop]])
  (:gen-class))

(defn -main []
  (let [system (start)]
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(stop system)))))
