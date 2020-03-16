(ns mercurius.core.configuration.system
  (:require [integrant.core :as ig]
            [taoensso.timbre :as log]
            [mercurius.core.controllers.mediator :refer [new-mediator]]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [new-in-memory-wallet-repo]]
            [mercurius.wallets.domain.use-cases.deposit :refer [deposit-use-case]]
            [mercurius.wallets.domain.use-cases.withdraw :refer [withdraw-use-case]]
            [mercurius.trading.domain.use-cases.place-order :refer [place-order-use-case]]))

(def config
  {:mediator {:wallets/deposit (ig/ref :use-cases/deposit)
              :wallets/withdraw (ig/ref :use-cases/withdraw)
              :trading/place-order {:wallet-repo (ig/ref :use-cases/place-order)}}
   :use-cases/deposit (ig/ref :wallets.repositories/in-memory)
   :use-cases/withdraw (ig/ref :wallets.repositories/in-memory)
   :use-cases/place-order (ig/ref :wallets.repositories/in-memory)
   :wallets.repositories/in-memory {}})

(defmethod ig/init-key :use-cases/deposit [_ repo]
  (deposit-use-case repo))

(defmethod ig/init-key :use-cases/withdraw [_ repo]
  (withdraw-use-case repo))

(defmethod ig/init-key :use-cases/place-order [_ repo]
  (place-order-use-case repo))

(defmethod ig/init-key :mediator [_ handlers]
  (log/info "Starting mediator for use cases" (keys handlers))
  (new-mediator handlers))

(defmethod ig/init-key :wallets.repositories/in-memory [_ _]
  (log/info "Starting in memory wallet repository")
  (new-in-memory-wallet-repo))

(defn start []
  (ig/init config))

(defn stop []
  (ig/halt!))

(comment
  (start)
  (stop))
