(ns mercurius.core.adapters.configuration.system
  (:require [taoensso.timbre :as log]
            [mercurius.core.adapters.controllers.mediator :refer [new-mediator dispatch]]
            [mercurius.core.adapters.controllers.mediator.middleware.logger :refer [logger]]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [new-in-memory-wallet-repo]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [load-wallet save-wallet fetch-wallet get-user-wallets]]
            [mercurius.wallets.domain.use-cases.deposit :refer [new-deposit-use-case]]
            [mercurius.wallets.domain.use-cases.withdraw :refer [new-withdraw-use-case]]
            [mercurius.wallets.domain.use-cases.get-wallet :refer [new-get-wallet-use-case]]
            [mercurius.wallets.domain.use-cases.get-wallets :refer [new-get-wallets-use-case]]
            [mercurius.trading.adapters.repositories.in-memory-order-book-repository :refer [new-in-memory-order-book-repo]]
            [mercurius.trading.adapters.processes.trade-finder :refer [start-trade-finder stop-trade-finder]]
            [mercurius.trading.domain.repositories.order-book-repository :refer [insert-order update-order remove-order get-bids-asks get-order-book]]
            [mercurius.trading.domain.use-cases.place-order :refer [new-place-order-use-case]]
            [mercurius.trading.domain.use-cases.get-order-book :refer [new-get-order-book-use-case]]
            [mercurius.trading.domain.use-cases.execute-trades :refer [new-execute-trades-use-case]]))

(defn start
  "Injects all the dependencies into the respective components and starts the system.
  Returns a map containing the components needed to drive the system.
  The resources that need to be freed when stopping the system should also be there."
  []
  (log/info "Starting system ...")

  (let [;; Repositories
        wallet-repo (new-in-memory-wallet-repo)
        order-book-repo (new-in-memory-order-book-repo)

        ;; Repository functions
        load-wallet (partial load-wallet wallet-repo)
        save-wallet (partial save-wallet wallet-repo)
        fetch-wallet (partial fetch-wallet wallet-repo)
        get-user-wallets (partial get-user-wallets wallet-repo)
        insert-order (partial insert-order order-book-repo)
        update-order (partial update-order order-book-repo)
        remove-order (partial remove-order order-book-repo)
        get-bids-asks (partial get-bids-asks order-book-repo)
        get-order-book (partial get-order-book order-book-repo)

        ;; Use cases
        deposit-use-case (new-deposit-use-case {:load-wallet load-wallet
                                                :save-wallet save-wallet})
        withdraw-use-case (new-withdraw-use-case {:fetch-wallet fetch-wallet
                                                  :save-wallet save-wallet})
        get-wallet-use-case (new-get-wallet-use-case {:fetch-wallet fetch-wallet})
        get-wallets-use-case (new-get-wallets-use-case {:get-user-wallets get-user-wallets})
        place-order-use-case (new-place-order-use-case {:fetch-wallet fetch-wallet
                                                        :save-wallet save-wallet
                                                        :insert-order insert-order})
        get-order-book-use-case (new-get-order-book-use-case {:get-order-book get-order-book})
        execute-trades-use-case (new-execute-trades-use-case {:get-bids-asks get-bids-asks
                                                              :update-order update-order
                                                              :remove-order remove-order
                                                              :fetch-wallet fetch-wallet
                                                              :load-wallet load-wallet
                                                              :save-wallet save-wallet})

        ;; Background processes
        trade-finder (start-trade-finder {:execute-trades execute-trades-use-case
                                          :run-every-ms 0})

        ;; Controllers
        mediator (new-mediator {:deposit deposit-use-case
                                :withdraw withdraw-use-case
                                :get-wallet get-wallet-use-case
                                :get-wallets get-wallets-use-case
                                :place-order place-order-use-case
                                :get-order-book get-order-book-use-case
                                :execute-trades execute-trades-use-case}
                               [logger])]

    {:dispatch (partial dispatch mediator)
     :trade-finder trade-finder}))

(defn stop [{:keys [trade-finder] :as system}]
  (when system
    (log/info "Stopping system ...")
    (stop-trade-finder trade-finder))
  nil)
