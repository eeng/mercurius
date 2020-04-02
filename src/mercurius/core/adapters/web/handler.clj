(ns mercurius.core.adapters.web.handler
  (:require [reitit.ring :as ring]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [mercurius.core.adapters.web.helpers :refer [ok]]
            [mercurius.core.adapters.web.index :refer [index]]))

(defn status [_req]
  (ok "System is online!"))

(defn router [{{:keys [ring-ajax-get-or-ws-handshake ring-ajax-post]} :sente}]
  (ring/router
   [["/" {:get index}]
    ["/status" {:get status}]
    ["/chsk" {:get ring-ajax-get-or-ws-handshake
              :post ring-ajax-post}]]))

(defn handler [{:keys [session-key] :as deps}]
  (-> (ring/ring-handler (router deps)
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
  ((handler {:sente {:ring-ajax-get-or-ws-handshake identity :ring-ajax-post identity}})
   {:request-method :get :uri "/"}))
