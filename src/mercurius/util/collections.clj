(ns mercurius.util.collections)

(defn detect [pred coll]
  (->> coll (filter pred) first))

(defn indexes-where
  [pred? coll]
  (keep-indexed #(when (pred? %2) %1) coll))

(defn update-where
  [v pred? f & args]
  (if-let [i (first (indexes-where pred? v))]
    (assoc v i (apply f (v i) args))
    v))

(comment
  (indexes-where pos? [-1 3 -2 4])
  (update-where [-1 3 -2 4] pos? inc))
