(ns mercurius.core.domain.use-cases.mediator
  (:require [slingshot.slingshot :refer [throw+]]))

(defrecord Mediator [handlers middleware])
(defrecord Request [type data])

(defn new-mediator [handlers & [middleware]]
  (Mediator. handlers (or middleware [])))

(defn- build-pipeline [use-case-handler middleware]
  ((apply comp middleware) use-case-handler))

(defn dispatch
  "This function is the main entry point to the domain. 
   Dispatchs commands and queries to the use cases, passing them through a pipeline of middleware,
   allowing to intercept the requests to apply cross-cutting concerns like logging."
  ([deps request-type] (dispatch deps request-type {}))
  ([{:keys [handlers middleware]} request-type request-data]
   (let [request (Request. request-type request-data)
         use-case (or (get handlers request-type)
                      (throw+ {:type ::use-case-not-found :request request}))
         use-case-handler (fn [request]
                            (use-case (:data request)))
         pipeline (build-pipeline use-case-handler middleware)]
     (pipeline request))))
