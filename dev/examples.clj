(ns examples
  (:require [integrant.core :as ig]
            [mercurius.system :as system]
            [mercurius.wallets.domain.use-cases.deposit :refer [deposit]]))

(comment
  (def system (ig/init system/config))

  (let [wallet-repo (system :wallets/repository)]
    (deposit wallet-repo {:amount 100})
    (deposit wallet-repo {:amount 50}))

  (ig/halt! system))
