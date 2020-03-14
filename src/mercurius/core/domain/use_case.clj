(ns mercurius.core.domain.use-case)

(defprotocol UseCase
  (execute [this command]))
