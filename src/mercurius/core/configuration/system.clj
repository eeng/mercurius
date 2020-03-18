(ns mercurius.core.configuration.system
  (:require [integrant.core :as ig]
            [taoensso.timbre :as log]
            [mercurius.core.controllers.mediator :refer [new-mediator]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [load-wallet save-wallet]]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [new-in-memory-wallet-repo]]
            [mercurius.wallets.domain.use-cases.deposit :refer [new-deposit-use-case]]
            [mercurius.wallets.domain.use-cases.withdraw :refer [withdraw-use-case]]
            [mercurius.trading.domain.use-cases.place-order :refer [place-order-use-case]]
            [mercurius.trading.domain.use-cases.get-order-book :refer [get-order-book-use-case]]
            [mercurius.trading.adapters.repositories.in-memory-order-book-repository :refer [new-in-memory-order-book-repo]]))

(def config
  {:mediator {:deposit (ig/ref :use-cases/deposit)
              :withdraw (ig/ref :use-cases/withdraw)
              :place-order (ig/ref :use-cases/place-order)
              :get-order-book (ig/ref :use-cases/get-order-book)}
   :use-cases/deposit {:load-wallet (ig/ref :wallets.repositories/load-wallet)
                       :save-wallet (ig/ref :wallets.repositories/save-wallet)}
   :use-cases/withdraw {:repo (ig/ref :wallets.repositories/in-memory)}
   :use-cases/place-order {:wallet-repo (ig/ref :wallets.repositories/in-memory)
                           :order-book-repo (ig/ref :trading.repositories/in-memory)}
   :use-cases/get-order-book {:repo (ig/ref :trading.repositories/in-memory)}
   :wallets.repositories/load-wallet (ig/ref :wallets.repositories/in-memory)
   :wallets.repositories/save-wallet (ig/ref :wallets.repositories/in-memory)
   :wallets.repositories/in-memory {}
   :trading.repositories/in-memory {}})

(defmethod ig/init-key :use-cases/deposit [_ deps]
  (new-deposit-use-case deps))

(defmethod ig/init-key :use-cases/withdraw [_ deps]
  (withdraw-use-case deps))

(defmethod ig/init-key :use-cases/place-order [_ deps]
  (place-order-use-case deps))

(defmethod ig/init-key :use-cases/get-order-book [_ deps]
  (get-order-book-use-case deps))

(defmethod ig/init-key :mediator [_ handlers]
  (log/info "Starting mediator for use cases" (keys handlers))
  (new-mediator handlers))

(defmethod ig/init-key :wallets.repositories/in-memory [_ _]
  (log/info "Starting in memory wallet repository")
  (new-in-memory-wallet-repo))

(defmethod ig/init-key :trading.repositories/in-memory [_ _]
  (log/info "Starting in memory order book repository")
  (new-in-memory-order-book-repo))

(defmethod ig/init-key :wallets.repositories/load-wallet [_ repo]
  (partial load-wallet repo))

(defmethod ig/init-key :wallets.repositories/save-wallet [_ repo]
  (partial save-wallet repo))

(defn start []
  (ig/init config))

(defn stop []
  (ig/halt!))

(comment
  (start)
  (stop))
