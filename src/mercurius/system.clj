(ns mercurius.system
  (:require [integrant.core :as ig]
            [taoensso.timbre :as log]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [make-in-memory-wallet-repo]]))

(def config
  {:wallets/repository {}})

(defmethod ig/init-key :wallets/repository [_ _]
  (log/info "Using InMemoryWalletRepository")
  (make-in-memory-wallet-repo))
