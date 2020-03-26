(ns mercurius.support.fixtures
  (:require [mercurius.core.adapters.configuration.system :refer [start stop]]))

(defmacro with-system
  "Starts the system and makes sure it's stopped afterward.
  The bindings works like let where the second argument are passed to start (not implemented yet)."
  [bindings & body]
  `(let [system# (start)]
     (try
       (let [~(first bindings) system#]
         ~@body)
       (finally
         (stop system#)))))
