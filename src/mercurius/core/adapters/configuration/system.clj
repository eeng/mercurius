(ns mercurius.core.adapters.configuration.system
  (:require [taoensso.timbre :as log]
            [mercurius.core.adapters.configuration.logging :refer [configure-logger]]
            [mercurius.core.adapters.controllers.mediator :refer [new-mediator dispatch]]
            [mercurius.core.adapters.controllers.mediator.middleware.logger :refer [logger]]
            [mercurius.core.adapters.messaging.channel-based-event-bus :refer [new-channel-based-event-bus]]
            [mercurius.core.domain.messaging.event-bus :refer [publish-event subscribe]]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [new-in-memory-wallet-repo]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [load-wallet save-wallet fetch-wallet get-user-wallets calculate-monetary-base]]
            [mercurius.wallets.domain.use-cases.deposit :refer [new-deposit-use-case]]
            [mercurius.wallets.domain.use-cases.withdraw :refer [new-withdraw-use-case]]
            [mercurius.wallets.domain.use-cases.get-wallet :refer [new-get-wallet-use-case]]
            [mercurius.wallets.domain.use-cases.get-wallets :refer [new-get-wallets-use-case]]
            [mercurius.wallets.domain.use-cases.calculate-monetary-base :refer [new-calculate-monetary-base-use-case]]
            [mercurius.trading.adapters.repositories.in-memory-order-book-repository :refer [new-in-memory-order-book-repo]]
            [mercurius.trading.adapters.repositories.in-memory-ticker-repository :refer [new-in-memory-ticker-repo]]
            [mercurius.trading.adapters.processes.trade-finder :refer [start-trade-finder]]
            [mercurius.trading.adapters.processes.ticker-updater :refer [new-ticker-updater]]
            [mercurius.trading.domain.repositories.order-book-repository :refer [insert-order update-order remove-order get-bids-asks get-order-book]]
            [mercurius.trading.domain.repositories.ticker-repository :refer [update-ticker get-tickers]]
            [mercurius.trading.domain.use-cases.place-order :refer [new-place-order-use-case]]
            [mercurius.trading.domain.use-cases.get-order-book :refer [new-get-order-book-use-case]]
            [mercurius.trading.domain.use-cases.execute-trades :refer [new-execute-trades-use-case]]
            [mercurius.trading.domain.use-cases.update-ticker :refer [new-update-ticker-use-case]]
            [mercurius.trading.domain.use-cases.get-tickers :refer [new-get-tickers-use-case]]))

(defn start
  "Injects all the dependencies into the respective components and starts the system.
  Returns a map containing the components needed to drive the system.
  The resources that need to be freed when stopping the system should also be there."
  []
  (configure-logger)
  (log/info "Starting system")

  (let [;; Repositories
        wallet-repo (new-in-memory-wallet-repo)
        load-wallet (partial load-wallet wallet-repo)
        save-wallet (partial save-wallet wallet-repo)
        fetch-wallet (partial fetch-wallet wallet-repo)
        get-user-wallets (partial get-user-wallets wallet-repo)
        calculate-monetary-base (partial calculate-monetary-base wallet-repo)

        order-book-repo (new-in-memory-order-book-repo)
        insert-order (partial insert-order order-book-repo)
        update-order (partial update-order order-book-repo)
        remove-order (partial remove-order order-book-repo)
        get-bids-asks (partial get-bids-asks order-book-repo)
        get-order-book (partial get-order-book order-book-repo)

        ticker-repo (new-in-memory-ticker-repo)
        update-ticker (partial update-ticker ticker-repo)
        get-tickers (partial get-tickers ticker-repo)

        ;; Event Bus
        event-bus (new-channel-based-event-bus)
        publish-event (partial publish-event event-bus)
        subscribe (partial subscribe event-bus)

        ;; Use Cases
        deposit-use-case (new-deposit-use-case
                          {:load-wallet load-wallet
                           :save-wallet save-wallet})
        withdraw-use-case (new-withdraw-use-case
                           {:fetch-wallet fetch-wallet
                            :save-wallet save-wallet})
        get-wallet-use-case (new-get-wallet-use-case
                             {:fetch-wallet fetch-wallet})
        get-wallets-use-case (new-get-wallets-use-case
                              {:get-user-wallets get-user-wallets})
        calculate-monetary-base-use-case (new-calculate-monetary-base-use-case
                                          {:calculate-monetary-base calculate-monetary-base})
        place-order-use-case (new-place-order-use-case
                              {:fetch-wallet fetch-wallet
                               :save-wallet save-wallet
                               :insert-order insert-order
                               :publish-event publish-event})
        get-order-book-use-case (new-get-order-book-use-case
                                 {:get-order-book get-order-book})
        execute-trades-use-case (new-execute-trades-use-case
                                 {:get-bids-asks get-bids-asks
                                  :update-order update-order
                                  :remove-order remove-order
                                  :fetch-wallet fetch-wallet
                                  :load-wallet load-wallet
                                  :save-wallet save-wallet
                                  :publish-event publish-event})
        update-ticker-use-case (new-update-ticker-use-case
                                {:update-ticker update-ticker})
        get-tickers-use-case (new-get-tickers-use-case
                              {:get-tickers get-tickers})

        ;; Background Processes
        trade-finder (start-trade-finder {:execute-trades execute-trades-use-case
                                          :run-every-ms 0})
        _ (new-ticker-updater {:subscribe subscribe
                               :update-ticker update-ticker-use-case})

        ;; Controllers
        mediator (new-mediator {:deposit deposit-use-case
                                :withdraw withdraw-use-case
                                :get-wallet get-wallet-use-case
                                :get-wallets get-wallets-use-case
                                :place-order place-order-use-case
                                :get-order-book get-order-book-use-case
                                :execute-trades execute-trades-use-case
                                :calculate-monetary-base calculate-monetary-base-use-case
                                :get-tickers get-tickers-use-case}
                               [logger])]

    {:dispatch (partial dispatch mediator)
     :order-book-repo order-book-repo
     :wallet-repo wallet-repo
     :trade-finder trade-finder
     :event-bus event-bus}))

(defn stop [{:keys [trade-finder event-bus] :as system}]
  (when system
    (log/info "Stopping system")
    (.close trade-finder)
    (.close event-bus))
  nil)
