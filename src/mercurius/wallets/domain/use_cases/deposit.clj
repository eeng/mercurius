(ns mercurius.wallets.domain.use-cases.deposit
  (:require [clojure.spec.alpha :as s]
            [mercurius.wallets.domain.entities.wallet :as wallet]
            [mercurius.accounts.domain.entities.user :as user]))

(s/def ::user-id ::user/id)
(s/def ::currency ::wallet/currency)
(s/def ::amount number?)
(s/def ::command (s/keys :req-un [::user-id ::currency ::amount]))

(defn new-deposit-use-case
  "Returns a use case that allows to deposit some amount into a wallet."
  [{:keys [load-wallet save-wallet publish-events]}]
  (fn [{:keys [user-id currency amount] :as command}]
    (s/assert ::command command)
    (let [{:keys [last-events] :as wallet}
          (-> (load-wallet user-id currency)
              (wallet/deposit amount))]
      (save-wallet wallet)
      (publish-events last-events))))
