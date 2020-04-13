(ns mercurius.accounts.domain.use-cases.authenticate
  (:require [clojure.spec.alpha :as s]
            [mercurius.accounts.domain.repositories.user-repository :refer [find-by-username]]))

(s/def ::username string?)
(s/def ::password string?)
(s/def ::command (s/keys :req-un [::username ::password]))

(defn- check-password [user password]
  (when (= (:password user) password)
    user))

(defn new-authenticate-use-case
  "Fake implementation, just checks if the credentials match someone on the hardcoded db. 
  In a real scenario we would fetch the user from a repository and check against the encrypted password."
  [{:keys [user-repo]}]
  (fn [{:keys [username password] :as command}]
    (s/assert ::command command)
    (if-let [user (some-> (find-by-username user-repo username)
                          (check-password password))]
      [:ok user]
      [:error :invalid-credentials])))
