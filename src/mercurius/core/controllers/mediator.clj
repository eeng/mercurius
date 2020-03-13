(ns mercurius.core.controllers.mediator
  (:require [mercurius.core.domain.use-case :as use-case]
            [slingshot.slingshot :refer [throw+]]
            [mercurius.core.controllers.mediator.middleware.logger :refer [logger]]))

(defrecord Mediator [handlers])

(defn new-mediator [handlers]
  (Mediator. handlers))

(defn dispatch [{:keys [handlers]} {:keys [type] :as command}]
  (let [use-case (or (get handlers type)
                     (throw+ {:type ::use-case-not-found :command command}))
        use-case-handler (fn [command] (use-case/execute use-case command))
        pipeline (-> use-case-handler logger)]
    (pipeline command)))
