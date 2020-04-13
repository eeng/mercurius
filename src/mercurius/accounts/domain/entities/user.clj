(ns mercurius.accounts.domain.entities.user
  (:require [clojure.spec.alpha :as s]
            [mercurius.util.uuid :refer [uuid]]))

(s/def ::id string?)

(defn new-user []
  {:id (uuid)})
