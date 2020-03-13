(ns mercurius.core.domain.use-case
  (:require [clojure.spec.alpha :as s]))

(defprotocol UseCase
  (execute [this command]))

(defmulti request-type :type)
(s/def :use-case/request (s/multi-spec request-type :type))
