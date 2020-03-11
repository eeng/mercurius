(ns mercurius.system
  (:require [integrant.core :as ig]
            [mercurius.wallets.domain.use-cases.deposit :refer [deposit]]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [make-in-memory-wallet-repo]]))

(def config
  {:wallets/repository {}})

(defmethod ig/init-key :wallets/repository [_ _]
  (println "Starting InMemory")
  (make-in-memory-wallet-repo))

(comment
  (let [system (ig/init config)]
    (deposit (system :wallets/repository) {:amount 100})
    (ig/halt! system)))
