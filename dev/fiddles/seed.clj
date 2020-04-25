(ns fiddles.seed
  (:require [user :refer [system reset]]
            [mercurius.core.configuration.seed :refer [seed]]))

(comment
  (do
    (reset)
    (seed system)))
