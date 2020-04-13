(ns mercurius.core.infraestructure.web.handler
  (:require [reitit.ring :as ring]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [mercurius.core.infraestructure.web.index :refer [index]]
            [mercurius.accounts.adapters.controllers.auth-controller :as auth]))

(defn router [{{:keys [ring-ajax-get-or-ws-handshake ring-ajax-post]} :sente
               :keys [dispatch]}]
  (ring/router
   [["/" {:get index}]
    ["/login" {:post {:handler (partial auth/login {:dispatch dispatch})
                      :muuntaja m/instance
                      :middleware [muuntaja/format-middleware]}}]
    ["/logout" {:post {:handler auth/logout}}]
    ["/chsk" {:get ring-ajax-get-or-ws-handshake
              :post ring-ajax-post}]]))

(defn handler [{:keys [session-key] :as deps}]
  (-> (ring/ring-handler (router deps)
                         (ring/routes
                          (ring/create-resource-handler {:path "/"})
                          (ring/create-default-handler)))
      ;; TODO this are only needed for sente, check if we can use reitit middleware to apply them only to the /chsk route
      ;; Required for Sente comms
      wrap-keyword-params
      wrap-params
      ;; Required for Sente CSRF protection
      wrap-anti-forgery
      (wrap-session {:store (cookie-store {:key session-key})})))

(comment
  (def handle (handler {:sente {:ring-ajax-get-or-ws-handshake identity :ring-ajax-post identity}}))
  (handle {:request-method :get :uri "/"})
  ; Disable the wrap-anti-forgery for this one
  (handle {:request-method :post
           :uri "/login"
           :body "{:username \"max\"}"
           :headers {"accept" "application/edn" "content-type" "application/edn"}}))
