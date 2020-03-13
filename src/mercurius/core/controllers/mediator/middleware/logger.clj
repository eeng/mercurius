(ns mercurius.core.controllers.mediator.middleware.logger
  (:require [taoensso.timbre :as log]))

(defn logger [next-handler]
  (fn [command]
    (log/info "Executing command" command)
    (let [start (System/currentTimeMillis)
          result (next-handler command)
          duration (- (System/currentTimeMillis) start)]
      (log/info "Finished command" (:type command) "in" duration "ms")
      result)))
