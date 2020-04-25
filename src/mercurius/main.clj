(ns mercurius.main
  (:require [mercurius.core.configuration.system :refer [start stop]]
            [mercurius.core.configuration.seed :refer [seed]])
  (:gen-class))

(defn -main [& args]
  (let [system (start)]
    (when (= args ["seed"])
      (seed system))
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(stop system)))))

(comment
  (-main)
  (-main "seed"))
