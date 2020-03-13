(ns examples
  (:require [integrant.core :as ig]
            [mercurius.system :as system]
            [mercurius.wallets.domain.use-cases :refer [deposit withdraw]]))

(comment
  (def system (ig/init system/config))

  (let [wallet-repo (system :wallets/repository)]
    (deposit wallet-repo {:amount 100})
    (deposit wallet-repo {:amount 50})
    (withdraw wallet-repo {:amount 30}))

  (ig/halt! system))
