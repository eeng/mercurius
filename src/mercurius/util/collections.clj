(ns mercurius.util.collections)

(defn detect [pred coll]
  (->> coll (filter pred) first))

(defn sum-by [f coll]
  (->> coll (map f) (reduce +)))

(defn reverse-merge [m1 m2]
  (merge m2 m1))

(defn map-vals [f m]
  (reduce-kv (fn [res k v]
               (assoc res k (f v)))
             {}
             m))

(defn assoc-or
  "Create mapping from each key to val in map only if existing val is nil."
  ([m key val]
   (if (nil? (get m key))
     (assoc m key val)
     m))
  ([m key val & kvs]
   (let [m (assoc-or m key val)]
     (if kvs
       (recur m (first kvs) (second kvs) (nnext kvs))
       m))))
