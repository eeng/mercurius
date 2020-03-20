(ns mercurius.wallets.domain.use-cases.deposit
  (:require [clojure.spec.alpha :as s]
            [mercurius.wallets.domain.entities.wallet :as wallet]
            [mercurius.accounts.domain.entities.user :as user]))

;; TODO there is an inconsistency between ::user/id and :wallet/currency
(s/def ::user-id ::user/id)
(s/def ::currency :wallet/currency)
(s/def ::amount number?)
(s/def ::command (s/keys :req-un [::user-id ::currency ::amount]))

(defn new-deposit-use-case
  "Returns a use case that allows to deposit some amount into a wallet."
  [{:keys [load-wallet save-wallet]}]
  (fn [{:keys [user-id currency amount] :as command}]
    (s/assert ::command command)
    (-> (load-wallet user-id currency)
        (wallet/deposit amount)
        (save-wallet))))

(comment
  (let [deposit (new-deposit-use-case {:load-wallet (constantly {:balance 50})
                                       :save-wallet identity})]
    (deposit {:amount 10 :user-id 1 :currency "USD"})))
