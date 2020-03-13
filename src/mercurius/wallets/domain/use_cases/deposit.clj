(ns mercurius.wallets.domain.use-cases.deposit
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [load-wallet save-wallet]]
            [mercurius.wallets.domain.entities.wallet :as wallet]))

(defn deposit [repo {:keys [user-id currency amount]}]
  (as-> (load-wallet repo user-id currency) w
    (wallet/deposit w amount)
    (save-wallet repo w)))
