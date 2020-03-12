(ns mercurius.wallets.domain.repositories.wallet-repository)

(defprotocol WalletRepository
  (save-wallet [this wallet])
  (load-wallet [this user-id currency])
  (get-user-wallets [this user-id]))
