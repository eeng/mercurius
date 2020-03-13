(ns mercurius.wallets.domain.repositories.wallet-repository)

(defprotocol WalletRepository
  (save-wallet [this wallet]
    "Creates or update the wallet.")

  (load-wallet [this user-id currency]
    "Finds the wallet for the user-id and currency, or creates it if doesn't exists yet.")

  (get-user-wallets [this user-id]
    "Finds all the wallets for the specified user-id"))
