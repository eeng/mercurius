(ns mercurius.wallets.adapters.presenters.wallet-presenter)

(defn wallet-edn-presenter
  "Converts a Wallet record into a map ready to be send to the wire.
  Also, we always get a users' wallets so the user-id is redundant."
  [wallet]
  (dissoc wallet :user-id))
