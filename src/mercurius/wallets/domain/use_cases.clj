(ns mercurius.wallets.domain.use-cases
  (:require [mercurius.wallets.domain.repositories.wallet-repository :refer [load-wallet save-wallet]]
            [mercurius.wallets.domain.entities.wallet :as wallet]
            [clojure.spec.alpha :as s]))

(s/def ::user-id int?)
(s/def ::currency :wallet/currency)
(s/def ::amount number?)
(s/def ::deposit-command (s/keys :req-un [::user-id ::currency ::amount]))
(s/def ::withdraw-command ::deposit-command)

(defn deposit [repo {:keys [user-id currency amount] :as command}]
  (s/assert ::deposit-command command)
  (as-> (load-wallet repo user-id currency) w
    (wallet/deposit w amount)
    (save-wallet repo w)))

(defn withdraw [repo {:keys [user-id currency amount] :as command}]
  (s/assert ::withdraw-command command)
  (as-> (load-wallet repo user-id currency) w
    (wallet/withdraw w amount)
    (save-wallet repo w)))
