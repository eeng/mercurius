(ns mercurius.core.controllers.mediator.middleware.spec-checker
  (:require [clojure.spec.alpha :as s]))

(defn spec-checker [next-handler]
  (fn [request]
    (s/assert :use-case/request request)
    (next-handler request)))
