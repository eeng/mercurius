(ns mercurius.core.configuration.system
  (:require [taoensso.timbre :as log]
            [mercurius.core.controllers.mediator :refer [new-mediator]]
            [mercurius.core.controllers.mediator.middleware.logger :refer [logger]]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [new-in-memory-wallet-repo]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [load-wallet save-wallet fetch-wallet]]
            [mercurius.wallets.domain.use-cases.deposit :refer [new-deposit-use-case]]
            [mercurius.wallets.domain.use-cases.withdraw :refer [new-withdraw-use-case]]
            [mercurius.wallets.domain.use-cases.get-wallet :refer [new-get-wallet-use-case]]
            [mercurius.trading.adapters.repositories.in-memory-order-book-repository :refer [new-in-memory-order-book-repo]]
            [mercurius.trading.domain.repositories.order-book-repository :refer [insert-order]]
            [mercurius.trading.domain.use-cases.place-order :refer [new-place-order-use-case]]
            [mercurius.trading.domain.use-cases.get-order-book :refer [new-get-order-book-use-case]]
            [mercurius.trading.domain.use-cases.match-orders :refer [new-match-orders-use-case]]))

(defn start
  "Injects all the dependencies into the respective components and starts the system.
  Returns a map containing the components needed to drive the system.
  The resources that need to be freed when stopping the system should also be there."
  []
  (log/info "Starting system ...")

  (let [wallet-repo (new-in-memory-wallet-repo)
        order-book-repo (new-in-memory-order-book-repo)

        load-wallet (partial load-wallet wallet-repo)
        save-wallet (partial save-wallet wallet-repo)
        fetch-wallet (partial fetch-wallet wallet-repo)
        insert-order (partial insert-order order-book-repo)

        deposit-use-case (new-deposit-use-case {:load-wallet load-wallet
                                                :save-wallet save-wallet})
        withdraw-use-case (new-withdraw-use-case {:fetch-wallet fetch-wallet
                                                  :save-wallet save-wallet})
        get-wallet-use-case (new-get-wallet-use-case {:fetch-wallet fetch-wallet})
        place-order-use-case (new-place-order-use-case {:fetch-wallet fetch-wallet
                                                        :save-wallet save-wallet
                                                        :insert-order insert-order})

        get-order-book-use-case (new-get-order-book-use-case {:repo order-book-repo})
        match-orders-use-case (new-match-orders-use-case {:fetch-wallet fetch-wallet
                                                          :load-wallet load-wallet
                                                          :save-wallet save-wallet})

        mediator (new-mediator {:deposit deposit-use-case
                                :withdraw withdraw-use-case
                                :get-wallet get-wallet-use-case
                                :place-order place-order-use-case
                                :get-order-book get-order-book-use-case}
                               [logger])]

    {:mediator mediator}))

(defn stop [system]
  (when system
    (log/info "Stopping system ...")))