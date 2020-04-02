(ns mercurius.core.configuration.config)

(def default-config {:log-level :info
                     :port 5000
                     :session-key "adOzRKX&KWFK)rJ5"})

(def ^:dynamic *config-overrides* {})

(defn read-config []
  (merge default-config *config-overrides*))
