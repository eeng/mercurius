(ns mercurius.core.adapters.controllers.mediator.middleware.retrier
  (:require [mercurius.util.retry :refer [with-retry]]))

(defn retrier
  "Will retry the request when an optimistic concurrency check fails."
  [next-handler]
  (fn [request]
    (with-retry {:on :stale-object-error :times 2 :delay-ms 500}
      (next-handler request))))
