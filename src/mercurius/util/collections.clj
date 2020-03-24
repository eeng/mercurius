(ns mercurius.util.collections)

(defn detect [pred coll]
  (->> coll (filter pred) first))

(defn reverse-merge [m1 m2]
  (merge m2 m1))

(defn map-vals [f m]
  (reduce-kv (fn [res k v]
               (assoc res k (f v)))
             {}
             m))

(defn sum-by [f coll]
  (->> coll (map f) (reduce +)))

(comment
  (map-vals inc {:a 1 :b 2}))
