(ns mercurius.core.adapters.controllers.request-processor
  "Receives requests (commands and queries) coming from the UI
  and dispatches them through the mediator."
  (:require [taoensso.timbre :as log]
            [mercurius.core.adapters.controllers.pub-sub :refer [subscribe]]))

(defn- dispatch-request [dispatch request]
  (try
    [:ok (apply dispatch request)]
    (catch clojure.lang.ExceptionInfo e
      [:error (ex-data e)])
    (catch Exception e
      [:error (.getMessage e)])))

(defn start-request-processor [{:keys [pub-sub dispatch]}]
  (log/info "Starting request processor")
  (subscribe pub-sub
             :frontend/request
             (fn [[_ msg-data]]
               (log/trace "Received" msg-data)
               (dispatch-request dispatch msg-data))))
