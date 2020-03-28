(ns mercurius.support.helpers
  (:require [clojure.core.async :refer [timeout alts!!]]))

(defn recorded-calls [calls-chan expected-count & {:keys [wait-for] :or {wait-for 500}}]
  (loop [recorded []
         actual-count 0]
    (if (< actual-count expected-count)
      (let [[call _] (alts!! [calls-chan (timeout wait-for)])]
        (if call
          (recur (conj recorded call) (inc actual-count))
          recorded))
      recorded)))
