(ns mercurius.core.use-case)

(defprotocol UseCase
  (run [this command]))

