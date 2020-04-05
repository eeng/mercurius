(ns mercurius.core.domain.use-cases.mediator.middleware.logger
  (:require [taoensso.timbre :as log]))

(defn logger [next-handler]
  (fn [{:keys [type data] :as request}]
    (log/debug "Executing" type data)
    (let [start (System/currentTimeMillis)
          result (next-handler request)
          duration (- (System/currentTimeMillis) start)]
      (log/debug "Finished" type "in" duration "ms")
      result)))
