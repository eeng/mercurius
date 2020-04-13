(ns mercurius.accounts.adapters.repositories.in-memory-user-repository
  (:require [mercurius.accounts.domain.repositories.user-repository :refer [UserRepository find-by-username]]
            [mercurius.util.uuid :refer [uuid]]))

(def users [{:id (uuid) :username "user1" :password "secret"}
            {:id (uuid) :username "user2" :password "secret"}
            {:id (uuid) :username "user3" :password "secret"}])

(defrecord InMemoryUserRepository [db]
  UserRepository

  (find-by-username [_ username]
    (get @db username)))

(defn new-in-memory-user-repo []
  (InMemoryUserRepository. (atom (zipmap (map :username users) users))))

(comment
  (let [repo (new-in-memory-user-repo)]
    (find-by-username repo "user1")))
