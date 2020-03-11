(ns mercurius.lib.collections)

(defn detect [pred coll]
  (->> coll (filter pred) first))

