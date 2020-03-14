(ns mercurius.core.controllers.mediator
  (:require [mercurius.core.domain.use-case :refer [execute]]
            [slingshot.slingshot :refer [throw+]]
            [mercurius.core.controllers.mediator.middleware.logger :refer [logger]]))

(defrecord Mediator [handlers])

(defn new-mediator [handlers]
  (Mediator. handlers))

(defn- build-pipeline [use-case-handler]
  (-> use-case-handler logger))

(defn dispatch [{:keys [handlers]} {:keys [type] :as request}]
  (let [use-case (or (get handlers type)
                     (throw+ {:type ::use-case-not-found :request request}))
        pipeline (build-pipeline #(execute use-case %))]
    (pipeline request)))
