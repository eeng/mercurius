(ns mercurius.wallets.domain.repositories.wallet-repository)

(defprotocol WalletRepository
  (find-or-create-for [this user-id currency])
  (save [this wallet]))
