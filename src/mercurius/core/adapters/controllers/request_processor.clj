(ns mercurius.core.adapters.controllers.request-processor
  "Receives requests (commands and queries) coming from the UI
  and dispatches them through the mediator."
  (:require [taoensso.timbre :as log]))

(defn new-request-processor [{:keys [dispatch]}]
  (fn [[request-type request-data] context]
    (try
      [:ok (dispatch request-type (merge request-data context))]
      (catch clojure.lang.ExceptionInfo e
        (log/error e)
        [:error (ex-data e)])
      (catch Exception e
        (log/error e)
        [:error (.getMessage e)]))))
