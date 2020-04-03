(ns mercurius.util.number
  (:refer-clojure :exclude [rand]))

(defn round-to-decimal-places [number precision]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* number factor)) factor)))

(defn round-to-significant-figures [num digits]
  (let [n (- digits (-> num Math/log10 Math/floor inc))]
    (round-to-decimal-places num n)))

(defn rand
  "Return a random float between start (inclusive) and end (exclusive).
  * `start` defaults to 0
  * `end` defaults to 1"
  ([] (clojure.core/rand))
  ([end] (clojure.core/rand end))
  ([start end] (+ start (clojure.core/rand (- end start)))))
