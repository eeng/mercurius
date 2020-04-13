(ns mercurius.core.adapters.controllers.auth-controller
  (:require [mercurius.util.ring :refer [ok]]))

(defn login [{:keys [body-params session]}]
  (println ">>> login" (pr-str body-params))
  (let [user {:id (str "user-id for " (:username body-params))}
        session (assoc session :uid (:id user))]
    (-> (ok {:user user})
        (assoc :session session))))

(defn logout [{:keys [session]}]
  (-> (ok nil)
      (assoc :session (assoc session :uid nil))))
