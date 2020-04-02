(ns mercurius.core.adapters.web.server
  (:require [org.httpkit.server :refer [run-server]]
            [mercurius.core.adapters.web.handler :refer [handler]]
            [taoensso.timbre :as log]))

(defn start-web-server [{:keys [port] :or {port 3000} :as deps}]
  (log/info (format "Starting web server at http://localhost:%d" port))
  (run-server (handler deps) {:port port}))

(defn stop-web-server [stop-server]
  (log/info "Stopping web server")
  (stop-server :timeout 100))
