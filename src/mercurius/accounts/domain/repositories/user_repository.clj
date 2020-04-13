(ns mercurius.accounts.domain.repositories.user-repository)

(defprotocol UserRepository
  (find-by-username [this username]
    "Returns the user with the `username` or nil if it doesn't exists"))
