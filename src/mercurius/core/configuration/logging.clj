(ns mercurius.core.configuration.logging
  (:require [taoensso.timbre :as log]
            [clojure.string :as str]))

(defn- truncate [str length]
  (subs str (- (count str) length)))

(defn output-fn
  "Customize the default output as we don't need the hostname, nor the timestamp on dev (cooper already provides one)"
  [data]
  (let [{:keys [level ?err #_vargs msg_ ?ns-str ?file timestamp_ ?line]} data]
    (str
     (str (force timestamp_) " ")
     (str/upper-case (first (name level)))  " "
     "[" (truncate (str (or ?ns-str ?file "?") ":" (or ?line "?")) 15) "] - "
     (force msg_)
     (when-let [err ?err]
       (str "\n" (log/stacktrace err nil))))))

(defn configure-logger [{:keys [log-level]}]
  (log/merge-config!
   {:level log-level
    :output-fn output-fn
    :timestamp-opts {:pattern "yy-MM-dd HH:mm:ss.SSS"}}))
