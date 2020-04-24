(ns mercurius.support.helpers
  (:require [clojure.test :refer [assert-expr]]
            [matcher-combinators.test :refer [build-match-assert]]
            [matcher-combinators.core :as mc]
            [matcher-combinators.matchers :as m]
            [mercurius.core.configuration.system :refer [start stop]]))

(defn ref-trx [f]
  (dosync
   (f)))

(defmacro with-system
  "Starts the system and makes sure it's stopped afterward.
  The bindings works like let where the second argument are passed to start (not implemented yet)."
  [bindings & body]
  `(let [system# (start ~(last bindings))]
     (try
       (let [~(first bindings) system#]
         ~@body)
       (finally
         (stop system#)))))

(def ^:dynamic *retry-matcher-wait* 50)
(def ^:dynamic *retry-matcher-retries* 4)

(defrecord RetryMatcher [expected]
  mc/Matcher
  (match [this actual]
    (loop [n *retry-matcher-retries*]
      (let [{:matcher-combinators.result/keys [type] :as result}
            (mc/match (m/equals expected) @actual)]
        (if (and (pos? n) (= type :mismatch))
          (do
            (Thread/sleep (/ *retry-matcher-wait* *retry-matcher-retries*))
            (recur (dec n)))
          result)))))

(defmethod assert-expr 'match-eventually? [msg form]
  (build-match-assert 'match-eventually? {clojure.lang.IPersistentVector ->RetryMatcher} msg form))
