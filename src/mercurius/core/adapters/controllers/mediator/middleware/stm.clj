(ns mercurius.core.adapters.controllers.mediator.middleware.stm)

(defn stm
  "Wraps each request in a dosync block, necessary for the in-memory repositories using refs."
  [next-handler]
  (fn [request]
    (dosync
     (next-handler request))))
