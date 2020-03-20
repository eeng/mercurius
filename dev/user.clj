(ns user
  (:require [mercurius.core.adapters.configuration.system :as s]
            [clojure.tools.namespace.repl :as repl]))

(def system nil)

(defn start []
  (alter-var-root #'system (constantly (s/start)))
  :started)

(defn stop []
  (alter-var-root #'system s/stop)
  :stopped)

(defn reset []
  (stop)
  (repl/refresh :after 'user/start))

(defn reset-all []
  (stop)
  (repl/refresh-all :after 'user/start))

(comment
  (start)
  (stop)
  (reset)
  (reset-all))
