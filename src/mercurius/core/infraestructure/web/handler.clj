(ns mercurius.core.infraestructure.web.handler
  (:require [reitit.ring :as ring]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [mercurius.core.infraestructure.web.helpers :refer [ok]]
            [mercurius.core.infraestructure.web.index :refer [index]]))

(defn status [_req]
  (ok "System is online!"))

(defn login [{:keys [body-params session]}]
  (println ">>> login" body-params)
  (let [user {:id (str "user-id for " (:username body-params))}
        session (assoc session :uid (:id user))]
    (-> (ok {:user user})
        (assoc :session session))))

(defn logout [_]
  (-> (ok "signed out")
      (assoc :session nil)))

(defn router [{{:keys [ring-ajax-get-or-ws-handshake ring-ajax-post]} :sente}]
  (ring/router
   [["/" {:get index}]
    ["/login" {:post {:handler login
                      :muuntaja m/instance
                      :middleware [muuntaja/format-middleware]}}]
    ["/logout" {:delete {:handler logout}}]
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
  (def handle (handler {:sente {:ring-ajax-get-or-ws-handshake identity :ring-ajax-post identity}}))
  (handle {:request-method :get :uri "/"})
  (handle {:request-method :get :uri "/status"}))
