(ns mercurius.core.adapters.controllers.request-processor
  "Receives requests (commands and queries) coming from the UI
  and dispatches them through the mediator."
  (:require [taoensso.timbre :as log]))

(defn new-request-processor [{:keys [dispatch]}]
  (fn [request]
    (log/info "Received" request)
    (try
      [:ok (apply dispatch request)]
      (catch clojure.lang.ExceptionInfo e
        [:error (ex-data e)])
      (catch Exception e
        [:error (.getMessage e)]))))
