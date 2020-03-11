(ns mercurius.wallets.domain.repositories.wallet-repository)

(defprotocol WalletRepository
  (save [this wallet])
  (find-by-user-id [this user-id])
  (find-or-create-by-user-id-and-currency [this user-id currency]))
