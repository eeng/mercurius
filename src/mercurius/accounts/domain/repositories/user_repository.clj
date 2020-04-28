(ns mercurius.accounts.domain.repositories.user-repository)

(defprotocol UserRepository
  (add-user [this user]
    "Adds the user to the repository and returns it.")

  (find-by-username [this username]
    "Returns the user with the `username` or nil if it doesn't exists"))
