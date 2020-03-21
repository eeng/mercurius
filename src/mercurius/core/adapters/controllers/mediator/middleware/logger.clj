(ns mercurius.core.adapters.controllers.mediator.middleware.logger
  (:require [taoensso.timbre :as log]))

(defn logger [& {:keys [exclude] :or {exclude #{}}}]
  (fn [next-handler]
    (fn [{:keys [type data] :as request}]
      (if (contains? exclude type)
        (next-handler request)
        (do (log/info "Executing" type data)
            (let [start (System/currentTimeMillis)
                  result (next-handler request)
                  duration (- (System/currentTimeMillis) start)]
              (log/info "Finished" type "in" duration "ms")
              result))))))
