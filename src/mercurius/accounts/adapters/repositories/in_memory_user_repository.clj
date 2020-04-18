(ns mercurius.accounts.adapters.repositories.in-memory-user-repository
  (:require [mercurius.accounts.domain.repositories.user-repository :refer [UserRepository find-by-username]]
            [mercurius.util.uuid :refer [uuid]]))

(def users
  (map (fn [i]
         {:id (uuid) :username (str "user" i) :password "secret"})
       (range 1 5)))

(defrecord InMemoryUserRepository [db]
  UserRepository

  (find-by-username [_ username]
    (get @db username)))

(defn new-in-memory-user-repo []
  (InMemoryUserRepository. (atom (zipmap (map :username users) users))))

(comment
  (let [repo (new-in-memory-user-repo)]
    (find-by-username repo "user1")))
