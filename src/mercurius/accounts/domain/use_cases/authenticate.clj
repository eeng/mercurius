(ns mercurius.accounts.domain.use-cases.authenticate
  (:require [clojure.spec.alpha :as s]
            [mercurius.util.uuid :refer [uuid]]
            [mercurius.util.collections :refer [detect]]))

(def users-db [{:id (uuid) :username "user1" :password "secret"}
               {:id (uuid) :username "user2" :password "secret"}
               {:id (uuid) :username "user3" :password "secret"}])

(s/def ::username string?)
(s/def ::password string?)
(s/def ::command (s/keys :req-un [::username ::password]))

(defn new-authenticate-use-case
  "Fake implementation, just checks if the credentials match someone on the hardcoded db. 
  In a real scenario we would fetch the user from a repository and check against the encrypted password."
  []
  (fn [{:keys [username password] :as command}]
    (s/assert ::command command)
    (if-let [user (detect #(and (= (:username %) username)
                                (= (:password %) password))
                          users-db)]
      [:ok user]
      [:error :invalid-credentials])))

(comment
  (let [authenticate (new-authenticate-use-case)]
    (authenticate {:username "user1" :password "secret"})))
