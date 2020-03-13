(ns user
  (:require [integrant.repl :refer [go halt reset reset-all]]
            [mercurius.core.configuration.system :refer [config]]))

(integrant.repl/set-prep! (constantly config))

(comment
  (go)
  (halt)
  (reset)
  (reset-all))
