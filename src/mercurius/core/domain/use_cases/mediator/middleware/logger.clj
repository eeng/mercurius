(ns mercurius.core.domain.use-cases.mediator.middleware.logger
  (:require [taoensso.timbre :as log]))

(defn logger [next-handler]
  (fn [{:keys [type data] :as request}]
    (log/info "Executing" type data)
    (try
      (let [start (System/currentTimeMillis)
            result (next-handler request)
            duration (- (System/currentTimeMillis) start)]
        (log/info "Finished" type "in" duration "ms")
        result)
      (catch Exception e
        (log/error e)
        (throw e)))))
