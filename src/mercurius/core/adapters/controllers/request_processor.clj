(ns mercurius.core.adapters.controllers.request-processor
  "Receives requests (commands and queries) coming from the UI
  and dispatches them through the mediator. 
  Returns a tuple containing the status of the execution.")

(defn new-request-processor [{:keys [dispatch]}]
  (fn [[request-type request-data] context]
    (try
      [:ok (dispatch request-type (merge request-data context))]
      (catch Exception e
        (if (instance? clojure.lang.ExceptionInfo e)
          [:error {:type (:type (ex-data e))
                   :message (.getMessage e)}]
          [:error {:type :unexpected-error
                   :message (.getMessage e)}])))))
