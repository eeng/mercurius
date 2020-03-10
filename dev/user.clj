(ns user
  (:require [integrant.repl :refer [go halt reset reset-all]]
            [mercurius.system :as system]))

(integrant.repl/set-prep! (constantly system/config))

(comment
  (go)
  (halt)
  (reset)
  (reset-all))
