(ns mercurius.core.controllers.mediator
  (:require [mercurius.core.domain.use-case :as use-case]
            [slingshot.slingshot :refer [throw+]]
            [mercurius.core.controllers.mediator.middleware.logger :refer [logger]]
            [mercurius.wallets.domain.use-cases.deposit :refer [deposit-use-case]]
            [mercurius.wallets.domain.use-cases.withdraw :refer [withdraw-use-case]]))

(defrecord Mediator [handlers])

(defn new-mediator [{:keys [wallet-repo]}]
  (Mediator.
   {:wallets/deposit (deposit-use-case wallet-repo)
    :wallets/withdraw (withdraw-use-case wallet-repo)}))

(defn execute [{:keys [handlers]} {:keys [type] :as command}]
  (let [use-case (or (get handlers type)
                     (throw+ {:type ::use-case-not-found :command command}))
        use-case-handler (fn [command] (use-case/execute use-case command))
        pipeline (-> use-case-handler logger)]
    (pipeline command)))
