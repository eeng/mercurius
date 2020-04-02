(ns mercurius.core.adapters.web.handler
  (:require [reitit.ring :as ring]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [mercurius.core.adapters.web.helpers :refer [ok]]
            [mercurius.core.adapters.web.index :refer [index]]))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(defn status [_req]
  (ok "System is online!"))

#_(defn debug [handler]
    (fn [request]
      (println "cli:" (get-in request [:params :csrf-token])
               "ref:" (:anti-forgery-token request))
      (handler request)))

(defn router [dispatch]
  (ring/router
   [["/" {:get index}]
    ["/status" {:get status}]
    ["/chsk" {:get ring-ajax-get-or-ws-handshake
              :post ring-ajax-post}]]))

(defn handler [{:keys [dispatch session-key]}]
  (-> (ring/ring-handler (router dispatch)
                         (ring/routes
                          (ring/create-resource-handler {:path "/"})
                          (ring/create-default-handler)))
      ;; Required for Sente comms
      wrap-keyword-params
      wrap-params
      ;; Required for Sente CSRF protection
      wrap-anti-forgery
      (wrap-session {:store (cookie-store {:key session-key})})))

(comment
  ((handler identity) {:request-method :get :uri "/"}))
