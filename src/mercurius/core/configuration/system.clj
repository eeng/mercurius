(ns mercurius.core.configuration.system
  (:require [integrant.core :as ig]
            [taoensso.timbre :as log]
            [mercurius.core.configuration.logging :refer [configure-logger]]
            [mercurius.core.configuration.config :refer [read-config]]
            [mercurius.core.adapters.controllers.mediator :refer [new-mediator dispatch]]
            [mercurius.core.adapters.controllers.mediator.middleware.logger :refer [logger]]
            [mercurius.core.adapters.controllers.mediator.middleware.retrier :refer [retrier]]
            [mercurius.core.adapters.controllers.mediator.middleware.stm :refer [stm]]
            [mercurius.core.adapters.messaging.channel-based-event-bus :refer [start-channel-based-event-bus stop-channel-based-event-bus]]
            [mercurius.core.adapters.processes.activity-logger :refer [new-activity-logger]]
            [mercurius.core.domain.messaging.event-bus :refer [publish-event]]
            [mercurius.core.adapters.web.server :refer [start-web-server stop-web-server]]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [new-in-memory-wallet-repo]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [load-wallet save-wallet fetch-wallet get-user-wallets calculate-monetary-base]]
            [mercurius.wallets.domain.use-cases.deposit :refer [new-deposit-use-case]]
            [mercurius.wallets.domain.use-cases.withdraw :refer [new-withdraw-use-case]]
            [mercurius.wallets.domain.use-cases.transfer :refer [new-transfer-use-case]]
            [mercurius.wallets.domain.use-cases.get-wallet :refer [new-get-wallet-use-case]]
            [mercurius.wallets.domain.use-cases.get-wallets :refer [new-get-wallets-use-case]]
            [mercurius.wallets.domain.use-cases.calculate-monetary-base :refer [new-calculate-monetary-base-use-case]]
            [mercurius.trading.adapters.repositories.in-memory-order-book-repository :refer [new-in-memory-order-book-repo]]
            [mercurius.trading.adapters.repositories.in-memory-ticker-repository :refer [new-in-memory-ticker-repo]]
            [mercurius.trading.adapters.processes.trade-finder :refer [new-trade-finder]]
            [mercurius.trading.adapters.processes.ticker-updater :refer [new-ticker-updater]]
            [mercurius.trading.domain.repositories.order-book-repository :refer [insert-order update-order remove-order get-bids-asks get-bid-ask get-order-book]]
            [mercurius.trading.domain.repositories.ticker-repository :refer [update-ticker get-tickers]]
            [mercurius.trading.domain.use-cases.place-order :refer [new-place-order-use-case]]
            [mercurius.trading.domain.use-cases.get-order-book :refer [new-get-order-book-use-case]]
            [mercurius.trading.domain.use-cases.execute-trades :refer [new-execute-trades-use-case]]
            [mercurius.trading.domain.use-cases.update-ticker :refer [new-update-ticker-use-case]]
            [mercurius.trading.domain.use-cases.get-tickers :refer [new-get-tickers-use-case]]))

(defn build-assembly [{:keys [port]}]
  {:adapters/wallet-repo nil
   :adapters/order-book-repo nil
   :adapters/ticker-repo nil
   :adapters/event-bus nil
   :use-cases/deposit {:wallet-repo (ig/ref :adapters/wallet-repo)}
   :use-cases/withdraw {:wallet-repo (ig/ref :adapters/wallet-repo)}
   :use-cases/transfer {:wallet-repo (ig/ref :adapters/wallet-repo)}
   :use-cases/get-wallet {:wallet-repo (ig/ref :adapters/wallet-repo)}
   :use-cases/get-wallets {:wallet-repo (ig/ref :adapters/wallet-repo)}
   :use-cases/calculate-monetary-base {:wallet-repo (ig/ref :adapters/wallet-repo)}
   :use-cases/place-order {:wallet-repo (ig/ref :adapters/wallet-repo)
                           :order-book-repo (ig/ref :adapters/order-book-repo)
                           :event-bus (ig/ref :adapters/event-bus)}
   :use-cases/get-order-book {:order-book-repo (ig/ref :adapters/order-book-repo)}
   :use-cases/execute-trades {:order-book-repo (ig/ref :adapters/order-book-repo)
                              :transfer-use-case (ig/ref :use-cases/transfer)
                              :event-bus (ig/ref :adapters/event-bus)}
   :use-cases/update-ticker {:ticker-repo (ig/ref :adapters/ticker-repo)
                             :event-bus (ig/ref :adapters/event-bus)}
   :use-cases/get-tickers {:ticker-repo (ig/ref :adapters/ticker-repo)}
   :controllers/dispatch {:handlers {:deposit (ig/ref :use-cases/deposit)
                                     :withdraw (ig/ref :use-cases/withdraw)
                                     :transfer (ig/ref :use-cases/transfer)
                                     :get-wallet (ig/ref :use-cases/get-wallet)
                                     :get-wallets (ig/ref :use-cases/get-wallets)
                                     :calculate-monetary-base (ig/ref :use-cases/calculate-monetary-base)
                                     :place-order (ig/ref :use-cases/place-order)
                                     :get-order-book (ig/ref :use-cases/get-order-book)
                                     :execute-trades (ig/ref :use-cases/execute-trades)
                                     :update-ticker (ig/ref :use-cases/update-ticker)
                                     :get-tickers (ig/ref :use-cases/get-tickers)}
                          :middleware [logger retrier stm]}
   :processes/trade-finder {:event-bus (ig/ref :adapters/event-bus)
                            :dispatch (ig/ref :controllers/dispatch)}
   :processes/ticker-updater {:event-bus (ig/ref :adapters/event-bus)
                              :dispatch (ig/ref :controllers/dispatch)}
   :processes/activity-logger {:event-bus (ig/ref :adapters/event-bus)}
   :adapters/web-server {:dispatch (ig/ref :controllers/dispatch)
                         :port port}})

(defmethod ig/init-key :adapters/wallet-repo [_ _]
  (new-in-memory-wallet-repo))

(defmethod ig/init-key :adapters/order-book-repo [_ _]
  (new-in-memory-order-book-repo))

(defmethod ig/init-key :adapters/ticker-repo [_ _]
  (new-in-memory-ticker-repo))

(defmethod ig/init-key :adapters/event-bus [_ _]
  (start-channel-based-event-bus))

(defmethod ig/halt-key! :adapters/event-bus [_ event-bus]
  (stop-channel-based-event-bus event-bus))

(defmethod ig/init-key :use-cases/deposit [_ {:keys [wallet-repo]}]
  (new-deposit-use-case {:load-wallet (partial load-wallet wallet-repo)
                         :save-wallet (partial save-wallet wallet-repo)}))

(defmethod ig/init-key :use-cases/withdraw [_ {:keys [wallet-repo]}]
  (new-withdraw-use-case {:fetch-wallet (partial fetch-wallet wallet-repo)
                          :save-wallet (partial save-wallet wallet-repo)}))

(defmethod ig/init-key :use-cases/transfer [_ {:keys [wallet-repo]}]
  (new-transfer-use-case {:fetch-wallet (partial fetch-wallet wallet-repo)
                          :load-wallet (partial load-wallet wallet-repo)
                          :save-wallet (partial save-wallet wallet-repo)}))

(defmethod ig/init-key :use-cases/get-wallet [_ {:keys [wallet-repo]}]
  (new-get-wallet-use-case {:fetch-wallet (partial fetch-wallet wallet-repo)}))

(defmethod ig/init-key :use-cases/get-wallets [_ {:keys [wallet-repo]}]
  (new-get-wallets-use-case {:get-user-wallets (partial get-user-wallets wallet-repo)}))

(defmethod ig/init-key :use-cases/calculate-monetary-base [_ {:keys [wallet-repo]}]
  (new-calculate-monetary-base-use-case
   {:calculate-monetary-base (partial calculate-monetary-base wallet-repo)}))

(defmethod ig/init-key :use-cases/place-order [_ {:keys [wallet-repo order-book-repo event-bus]}]
  (new-place-order-use-case {:fetch-wallet (partial fetch-wallet wallet-repo)
                             :save-wallet (partial save-wallet wallet-repo)
                             :get-bid-ask (partial get-bid-ask order-book-repo)
                             :insert-order (partial insert-order order-book-repo)
                             :publish-event (partial publish-event event-bus)}))

(defmethod ig/init-key :use-cases/get-order-book [_ {:keys [order-book-repo]}]
  (new-get-order-book-use-case {:get-order-book (partial get-order-book order-book-repo)}))

(defmethod ig/init-key :use-cases/execute-trades [_ {:keys [order-book-repo transfer-use-case event-bus]}]
  (new-execute-trades-use-case {:get-bids-asks (partial get-bids-asks order-book-repo)
                                :update-order (partial update-order order-book-repo)
                                :remove-order (partial remove-order order-book-repo)
                                :transfer transfer-use-case
                                :publish-event (partial publish-event event-bus)}))

(defmethod ig/init-key :use-cases/update-ticker [_ {:keys [ticker-repo event-bus]}]
  (new-update-ticker-use-case {:update-ticker (partial update-ticker ticker-repo)
                               :publish-event (partial publish-event event-bus)}))

(defmethod ig/init-key :use-cases/get-tickers [_ {:keys [ticker-repo]}]
  (new-get-tickers-use-case {:get-tickers (partial get-tickers ticker-repo)}))

(defmethod ig/init-key :controllers/dispatch [_ {:keys [handlers middleware]}]
  (let [mediator (new-mediator handlers middleware)]
    (partial dispatch mediator)))

(defmethod ig/init-key :processes/trade-finder [_ {:keys [event-bus dispatch]}]
  (new-trade-finder {:event-bus event-bus
                     :execute-trades (partial dispatch :execute-trades)}))

(defmethod ig/init-key :processes/ticker-updater [_ {:keys [event-bus dispatch]}]
  (new-ticker-updater {:event-bus event-bus
                       :update-ticker (partial dispatch :update-ticker)}))

(defmethod ig/init-key :processes/activity-logger [_ {:keys [event-bus]}]
  (new-activity-logger {:event-bus event-bus}))

(defmethod ig/init-key :adapters/web-server [_ deps]
  (start-web-server deps))

(defmethod ig/halt-key! :adapters/web-server [_ server]
  (stop-web-server server))

(defn start
  "Injects all the dependencies into the respective components and starts the system.
  Returns a map containing all the components."
  ([] (start {}))
  ([{:keys [only]}]
   (let [config (read-config)
         assembly (build-assembly config)]
     (configure-logger config)
     (log/info "Starting system with config" (pr-str config))
     (if only
       (ig/init assembly only)
       (ig/init assembly)))))

(defn stop [system]
  (when system
    (log/info "Stopping system")
    (ig/halt! system))
  nil)

(comment
  (def system (start {:only [:controllers/dispatch]}))
  (stop system))
