(ns mercurius.core.adapters.controllers.use-case-controller
  "Receives use case's requests (commands and queries) coming from Sente
  and dispatches them through the mediator. 
  Returns a tuple containing the status of the execution.")

(defn new-use-case-controller [{:keys [dispatch]}]
  (fn [[request-type request-data] context]
    (try
      [:ok (dispatch request-type (merge request-data context))]
      (catch Exception e
        (if (and (instance? clojure.lang.ExceptionInfo e)
                 (:type (ex-data e)))
          [:error {:type (:type (ex-data e))
                   :message (.getMessage e)}]
          [:error {:type :unexpected-error
                   :message (.getMessage e)}])))))
