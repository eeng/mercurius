(ns mercurius.util.retry
  (:require [slingshot.slingshot :refer [try+ throw+]]
            [taoensso.timbre :as log]))

(defn retry [f {:keys [times on delay-ms] :or {times 1 on Exception} :as opts}]
  (if (zero? times)
    (f)
    (try+
     (f)
     (catch [:type on] e
       (when delay-ms
         (Thread/sleep delay-ms))
       (log/warn "Retrying due to" e)
       (retry f (assoc opts :times (dec times)))))))

(defmacro with-retry [opts & body]
  `(retry (fn [] ~@body) ~opts))

(comment
  (with-retry {:times 1 :on :stale-object-error :delay-ms 500}
    (println ">>> DOING")
    (throw+ {:type :stale-object-error})))
