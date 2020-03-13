(ns examples
  (:require [integrant.core :as ig]
            [mercurius.system :as system]
            [mercurius.wallets.domain.use-cases :refer [deposit withdraw]]))

(comment
  (def system (ig/init system/config))

  (def wallet-repo (system :wallets/repository))
  (deposit wallet-repo {:user-id 1 :amount 100 :currency "USD"})
  (deposit wallet-repo {:user-id 1 :amount 50 :currency "USD"})
  (withdraw wallet-repo {:user-id 1 :amount 30 :currency "USD"})
  (deposit wallet-repo {:user-id 2 :amount 100 :currency "USD"})

  (ig/halt! system))
