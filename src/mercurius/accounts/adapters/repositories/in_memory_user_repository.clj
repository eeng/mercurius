(ns mercurius.accounts.adapters.repositories.in-memory-user-repository
  (:require [mercurius.accounts.domain.repositories.user-repository :refer [UserRepository]]))

(defrecord InMemoryUserRepository [db]
  UserRepository

  (add-user [_ {:keys [username] :as user}]
    (swap! db assoc username user)
    user)

  (find-by-username [_ username]
    (get @db username)))

(defn new-in-memory-user-repo []
  (InMemoryUserRepository. (atom {})))
