(ns mercurius.util.collections)

(defn detect [pred coll]
  (->> coll (filter pred) first))
