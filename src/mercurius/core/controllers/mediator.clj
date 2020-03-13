(ns mercurius.core.controllers.mediator
  (:require [mercurius.core.domain.use-case :as use-case]
            [slingshot.slingshot :refer [throw+]]
            [mercurius.core.controllers.mediator.middleware.logger :refer [logger]]
            [mercurius.core.controllers.mediator.middleware.spec-checker :refer [spec-checker]]))

(defrecord Mediator [handlers])

(defn new-mediator [handlers]
  (Mediator. handlers))

(defn- build-pipeline [use-case-handler]
  (-> use-case-handler spec-checker logger))

(defn dispatch [{:keys [handlers]} {:keys [type] :as command}]
  (let [use-case (or (get handlers type)
                     (throw+ {:type ::use-case-not-found :command command}))
        use-case-handler (fn [command]
                           (use-case/execute use-case command))
        pipeline (build-pipeline use-case-handler)]
    (pipeline command)))
