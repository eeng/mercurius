(ns user
  (:require [clojure.tools.namespace.repl :as repl]
            [clojure.spec.test.alpha :as stest]
            [mercurius.core.configuration.system :as s]
            [mercurius.core.configuration.seed :as sd]))

(def system nil)

(defn start []
  (when-not system
    (alter-var-root #'system (constantly (s/start)))
    (stest/instrument))
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

(defn seed []
  (sd/seed system))

(comment
  (start)
  (stop)
  (reset)
  (reset-all))
