(ns mercurius.util.collections)

(defn detect [pred coll]
  (->> coll (filter pred) first))

(defn reverse-merge [m1 m2]
  (merge m2 m1))
