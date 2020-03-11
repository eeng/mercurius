(ns support.asserts)

(defn submap? [sm m]
  {:pre [(map? sm)]}
  (and (map? m)
       (= sm (select-keys m (keys sm)))))

(defn submaps? [submaps maps]
  (and (every? (fn [[sm m]] (submap? sm m))
               (map vector submaps maps))
       (= (count submaps) (count maps))))
