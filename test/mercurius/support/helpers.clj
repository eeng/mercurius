(ns mercurius.support.helpers
  (:require [clojure.core.async :refer [timeout alts!!]]
            [clojure.test :refer [assert-expr do-report is]]
            [mercurius.util.retry :refer [with-retry]]
            [mercurius.core.configuration.system :refer [start stop]]))

(defn recorded-calls [calls-chan expected-count & {:keys [wait-for] :or {wait-for 500}}]
  (loop [recorded []
         actual-count 0]
    (if (< actual-count expected-count)
      (let [[call _] (alts!! [calls-chan (timeout wait-for)])]
        (if call
          (recur (conj recorded call) (inc actual-count))
          recorded))
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

(def ^:dynamic *eventually-retries* 4)
(def ^:dynamic *eventually-delay-ms* 20)

(defmethod assert-expr 'eventually [msg [_ & expr]]
  `(do-report
    (loop [tries# *eventually-retries*]
      (if ~expr
        {:type :pass}
        (if (zero? tries#)
          (let [actual# (nth '~expr 1)]
            {:type :fail
             :message ~msg
             :expected (nth '~expr 1)
             :actual actual#})
          (do
            (Thread/sleep *eventually-delay-ms*)
            (recur (dec tries#))))))))
