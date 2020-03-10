(ns mercurius.system
  (:require [integrant.core :as ig]
            [mercurius.core.domain.use-case :refer [execute]]
            [mercurius.wallets.domain.use-cases.deposit]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository]))

(def config
  {:wallets.use-cases/deposit {:repo (ig/ref :wallets.repositories/in-memory)}
   :wallets.repositories/in-memory {}})

(comment
  (let [system (ig/init config)]
    (execute (system :wallets.use-cases/deposit) {:amount 100})
    (ig/halt! system)))

