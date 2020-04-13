(ns mercurius.core.presentation.util.string)

(defn parse-float [str]
  (let [num (js/parseFloat str)]
    (if (js/isNaN num)
      nil
      num)))
