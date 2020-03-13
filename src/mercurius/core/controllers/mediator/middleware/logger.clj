(ns mercurius.core.controllers.mediator.middleware.logger
  (:require [taoensso.timbre :as log]))

(defn logger [next-handler]
  (fn [request]
    (log/info "Executing" request)
    (let [start (System/currentTimeMillis)
          result (next-handler request)
          duration (- (System/currentTimeMillis) start)]
      (log/info "Finished" (:type request) "in" duration "ms")
      result)))
