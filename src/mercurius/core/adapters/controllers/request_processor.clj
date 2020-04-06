(ns mercurius.core.adapters.controllers.request-processor
  "Receives requests (commands and queries) coming from the UI
  and dispatches them through the mediator.")

(defn new-request-processor [{:keys [dispatch]}]
  (fn [request]
    (try
      [:ok (apply dispatch request)]
      (catch clojure.lang.ExceptionInfo e
        [:error (ex-data e)])
      (catch Exception e
        [:error (.getMessage e)]))))
