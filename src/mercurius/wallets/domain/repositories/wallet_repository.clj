(ns mercurius.wallets.domain.repositories.wallet-repository
  (:require [mercurius.wallets.domain.entities.wallet :refer [new-wallet]]))

(defprotocol WalletRepository
  (save-wallet [this wallet]
    "Creates or update the wallet.")

  (find-wallet [this user-id currency]
    "Finds the wallet for the user-id and currency. Returns nil if it doesn't exists.")

  (get-user-wallets [this user-id]
    "Finds all the wallets for the specified user-id.")

  (calculate-monetary-base [this]
    "Returns a map with the total amount of each currency in the system."))

(defn load-wallet
  "Finds the wallet for the user-id and currency. Returns a new one if doesn't exists yet."
  [repo user-id currency]
  (or (find-wallet repo user-id currency)
      (new-wallet {:user-id user-id :currency currency})))
