(ns mercurius.core.adapters.controllers.mediator.middleware.retrier
  (:require [mercurius.util.retry :refer [with-retry]]))

(defn retrier
  "Will retry the request when an optimistic concurrency check fails."
  [next-handler]
  (fn [request]
    (with-retry {:on :stale-object-error :times 2 :delay-ms 200 :jitter-factor 0.1}
      (next-handler request))))
