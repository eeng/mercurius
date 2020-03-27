(ns mercurius.core.domain.entities.event
  (:require [mercurius.util.uuid :refer [uuid]]
            [tick.alpha.api :as t]))

(defrecord Event [type id created-at data])

(defn new-event [type data]
  (map->Event {:type type
               :data data
               :id (uuid)
               :created-at (t/now)}))
