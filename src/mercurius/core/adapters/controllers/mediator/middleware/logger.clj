(ns mercurius.core.adapters.controllers.mediator.middleware.logger
  (:require [taoensso.timbre :as log]))

(defn logger [next-handler]
  (fn [{:keys [type data] :as request}]
    (log/debug "Executing" type data)
    (try
      (let [start (System/currentTimeMillis)
            result (next-handler request)
            duration (- (System/currentTimeMillis) start)]
        (log/debug "Finished" type "in" duration "ms")
        result)
      (catch Exception e
        (log/error "Error executing" type data)
        (log/error e)
        (throw e)))))
