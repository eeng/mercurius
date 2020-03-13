(ns mercurius.core.configuration.system
  (:require [integrant.core :as ig]
            [taoensso.timbre :as log]
            [mercurius.core.controllers.mediator :refer [new-mediator]]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [new-in-memory-wallet-repo]]))

(def config
  {:mediator {:wallet-repo (ig/ref :wallets/repository)}
   :wallets/repository {}})

(defmethod ig/init-key :mediator [_ deps]
  (log/info "Starting mediator")
  (new-mediator deps))

(defmethod ig/init-key :wallets/repository [_ _]
  (log/info "Starting in memory wallet repository")
  (new-in-memory-wallet-repo))

