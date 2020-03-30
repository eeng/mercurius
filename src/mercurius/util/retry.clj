(ns mercurius.util.retry
  (:require [slingshot.slingshot :refer [try+ throw+]]
            [taoensso.timbre :as log]))

(defn retry
  [f {:keys [on times delay-ms jitter-factor]
      :or {on Exception delay-ms 0 times 1 jitter-factor 0}
      :as opts}]
  (if (zero? times)
    (f)
    (try+
     (f)
     (catch [:type on] e
       (let [jitter-sign (rand-nth [-1 1])
             delay (+ delay-ms (rand (* delay-ms jitter-factor jitter-sign)))]
         (log/warn (format "Retrying after %.0f ms due to %s" delay e))
         (when (pos? delay)
           (Thread/sleep delay)))
       (retry f (assoc opts :times (dec times)))))))

(defmacro with-retry
  "If there is an ExceptionInfo with :type `on` it will retry the function the specified number of `times`.
  Options:
    * `:delay-ms` The number of ms to sleep between the retries.
    * `:jitter-factor` A random portion of the delay up to this factor will be added or substracted to the delay."
  [opts & body]
  `(retry (fn [] ~@body) ~opts))

(comment
  (with-retry {:times 2 :on :stale-object-error :delay-ms 500 :jitter-factor 0.1}
    (log/info ">>> DOING")
    (throw+ {:type :stale-object-error})))
