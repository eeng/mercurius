(ns mercurius.support.helpers
  (:require [clojure.core.async :refer [timeout alts!!]]
            [mercurius.core.configuration.system :refer [start stop]]))

(defn recorded-calls [calls-chan expected-count & {:keys [wait-for] :or {wait-for 500}}]
  (loop [recorded []
         actual-count 0]
    (if (< actual-count expected-count)
      (let [[call ch] (alts!! [calls-chan (timeout wait-for)])]
        (if call
          (recur (conj recorded call) (inc actual-count))
          (if (= ch calls-chan)
            recorded
            (throw (Exception. (str "Timeout on recorded-calls. So far we have: " (pr-str recorded)))))))
      recorded)))

(defn ref-trx [f]
  (dosync
   (f)))

(defmacro with-system
  "Starts the system and makes sure it's stopped afterward.
  The bindings works like let where the second argument are passed to start (not implemented yet)."
  [bindings & body]
  `(let [system# (start ~(last bindings))]
     (try
       (let [~(first bindings) system#]
         ~@body)
       (finally
         (stop system#)))))
