(ns mercurius.wallets.domain.use-cases.transfer
  (:require [clojure.spec.alpha :as s]
            [mercurius.wallets.domain.entities.wallet :as wallet :refer [transfer cancel-reservation]]
            [mercurius.accounts.domain.entities.user :as user]))

(s/def ::from ::user/id)
(s/def ::to ::user/id)
(s/def ::currency ::wallet/currency)
(s/def ::transfer-amount number?)
(s/def ::cancel-amount number?)
(s/def ::command (s/keys :req-un [::from ::to ::currency ::transfer-amount]
                         :opt-un [::cancel-amount]))

(defn new-transfer-use-case
  "Returns a use case that allows to transfer some amount between two users' wallets."
  [{:keys [load-wallet save-wallet]}]
  (fn [{:keys [from to currency transfer-amount cancel-amount] :as command
        :or {cancel-amount 0}}]
    (s/assert ::command command)
    (let [src (-> (load-wallet from currency)
                  (cancel-reservation cancel-amount))
          dst (load-wallet to currency)
          wallets (transfer src dst transfer-amount)]
      (doseq [wallet wallets]
        (save-wallet wallet)))))
