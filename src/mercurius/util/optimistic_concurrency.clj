(ns mercurius.util.optimistic-concurrency
  (:require [slingshot.slingshot :refer [throw+]]
            [mercurius.util.collections :refer [assoc-or]]))

(defn inc-version-if-same [new-val old-val]
  (let [old-val (assoc-or old-val :version 0)
        new-val (assoc-or new-val :version 0)]
    (if (= (:version new-val) (:version old-val))
      (update new-val :version inc)
      (throw+ {:type :stale-object-error
               :expected-version (:version old-val)
               :object new-val}))))

(defn optimistic-assoc [m k new-val]
  (update m k (partial inc-version-if-same new-val)))
