(ns mercurius.core.configuration.system
  (:require [integrant.core :as ig]
            [taoensso.timbre :as log]
            [mercurius.core.controllers.mediator :refer [new-mediator]]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [new-in-memory-wallet-repo]]
            [mercurius.wallets.domain.use-cases.deposit :refer [deposit-use-case]]
            [mercurius.wallets.domain.use-cases.withdraw :refer [withdraw-use-case]]))

(def config
  {:use-cases/deposit (ig/ref :wallets.repositories/in-memory)
   :use-cases/withdraw (ig/ref :wallets.repositories/in-memory)
   :mediator {:wallets/deposit (ig/ref :use-cases/deposit)
              :wallets/withdraw (ig/ref :use-cases/withdraw)}
   :wallets.repositories/in-memory {}})

(defmethod ig/init-key :use-cases/deposit [_ repo]
  (deposit-use-case repo))

(defmethod ig/init-key :use-cases/withdraw [_ repo]
  (withdraw-use-case repo))

(defmethod ig/init-key :mediator [_ handlers]
  (log/info "Starting mediator for use cases" (keys handlers))
  (new-mediator handlers))

(defmethod ig/init-key :wallets.repositories/in-memory [_ _]
  (log/info "Starting in memory wallet repository")
  (new-in-memory-wallet-repo))
