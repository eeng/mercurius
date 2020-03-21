(ns mercurius.wallets.domain.use-cases.get-wallets
  (:require [clojure.spec.alpha :as s]
            [mercurius.accounts.domain.entities.user :as user]))

(s/def ::user-id ::user/id)
(s/def ::command (s/keys :req-un [::user-id]))

(defn new-get-wallets-use-case
  "Returns a use case that allows to get a user's wallets."
  [{:keys [get-user-wallets]}]
  (fn [{:keys [user-id] :as command}]
    (s/assert ::command command)
    (get-user-wallets user-id)))
