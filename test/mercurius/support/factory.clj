(ns mercurius.support.factory
  (:require [mercurius.wallets.domain.entities.wallet :refer [new-wallet]]))

(defn build-wallet
  ([]
   (build-wallet {}))
  ([args]
   (new-wallet (merge {:currency "USD" :user-id 1} args))))
