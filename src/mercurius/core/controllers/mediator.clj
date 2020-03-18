(ns mercurius.core.controllers.mediator
  (:require [slingshot.slingshot :refer [throw+]]
            [mercurius.core.controllers.mediator.middleware.logger :refer [logger]]))

(defrecord Mediator [handlers])
(defrecord Request [type data])

(defn new-mediator [handlers]
  (Mediator. handlers))

(defn- build-pipeline [use-case-handler]
  (-> use-case-handler logger))

(defn dispatch
  "This function is the main entry point to the domain. 
   Dispatchs commands and queries to the use cases, passing them through a pipeline of middleware,
   allowing to intercept the requests to apply cross-cutting concerns like logging."
  [{:keys [handlers]} request-type request-data]
  (let [request (Request. request-type request-data)
        use-case (or (get handlers request-type)
                     (throw+ {:type ::use-case-not-found :request request}))
        use-case-handler (fn [request]
                           (use-case (:data request)))
        pipeline (build-pipeline use-case-handler)]
    (pipeline request)))
