(ns mercurius.core.adapters.web.server
  (:require [org.httpkit.server :refer [run-server]]
            [mercurius.core.adapters.web.handler :refer [handler]]
            [taoensso.timbre :as log]))

(defrecord WebServer [stop-server]
  java.io.Closeable

  (close [_]
    (log/info "Stopping web server")
    (stop-server :timeout 100)))

(defn new-web-server [{:keys [dispatch port] :or {port 3000}}]
  (log/info (format "Starting web server at http://localhost:%d" port))
  (WebServer.
   (run-server (handler dispatch) {:port port})))
