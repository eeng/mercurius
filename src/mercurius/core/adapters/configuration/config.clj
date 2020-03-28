(ns mercurius.core.adapters.configuration.config)

(def default-config {:log-level :info})

(def ^:dynamic *config-overrides* {})

(defn read-config []
  (merge default-config *config-overrides*))
