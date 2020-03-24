(ns mercurius.wallets.domain.repositories.wallet-repository
  (:require [slingshot.slingshot :refer [throw+]]
            [mercurius.wallets.domain.entities.wallet :refer [new-wallet]]))

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
  "Finds the wallet for the user-id and currency. Creates it if doesn't exists yet."
  [repo user-id currency]
  (or (find-wallet repo user-id currency)
      (save-wallet repo (new-wallet {:user-id user-id :currency currency}))))

(defn fetch-wallet
  "Finds the wallet for the user-id and currency. Throws :wallet/not-found if it doesn't exists."
  [repo user-id currency]
  (or (find-wallet repo user-id currency)
      (throw+ {:type :wallet/not-found :user-id user-id :currency currency})))
