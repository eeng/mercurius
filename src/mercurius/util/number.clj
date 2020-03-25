(ns mercurius.util.number)

(defn round-to-decimal-places [number precision]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* number factor)) factor)))

(defn round-to-significant-figures [num digits]
  (let [n (- digits (-> num Math/log10 Math/ceil))]
    (round-to-decimal-places num n)))
