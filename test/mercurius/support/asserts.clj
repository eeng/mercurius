(ns mercurius.support.asserts
  (:require [clojure.test :refer [assert-expr do-report]]))

(defn submap? [sm m]
  {:pre [(map? sm)]}
  (and (map? m)
       (= sm (select-keys m (keys sm)))))

(defmethod assert-expr 'thrown-with-data?
  [msg [_ data & body :as form]]
  `(do-report
    (try
      ~@body
     ;; We expect body to throw. If we get here, it's a failure
      {:type     :fail
       :message  (str (when ~msg (str ~msg ": "))
                      "expected exception")
       :expected '~form
       :actual   nil}
      (catch ~clojure.lang.ExceptionInfo e#
        (let [d# (ex-data e#)]
          (if (submap? ~data d#)
            {:type     :pass
             :message  ~msg
             :expected '~form
             :actual   e#}
            {:type     :fail
             :message  (str (when ~msg (str ~msg ": "))
                            "ex-data doesn't match")
             :expected '~data
             :actual   d#}))))))