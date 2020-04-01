(ns mercurius.core.configuration.config)

(def default-config {:log-level :info
                     :port 3000})

(def ^:dynamic *config-overrides* {})

(defn read-config []
  (merge default-config *config-overrides*))
