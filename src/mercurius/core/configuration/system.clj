(ns mercurius.core.configuration.system
  (:require [integrant.core :as ig]
            [taoensso.timbre :as log]
            [mercurius.core.configuration.logging :refer [configure-logger]]
            [mercurius.core.configuration.config :refer [read-config]]
            [mercurius.core.domain.use-cases.mediator :refer [new-mediator dispatch]]
            [mercurius.core.domain.use-cases.mediator.middleware.logger :refer [logger]]
            [mercurius.core.domain.use-cases.mediator.middleware.stm :refer [stm]]
            [mercurius.core.domain.messaging.event-bus :refer [emit]]
            [mercurius.core.adapters.messaging.pub-sub-event-bus :refer [new-pub-sub-event-bus]]
            [mercurius.core.infraestructure.messaging.channel-based-pub-sub :refer [start-channel-based-pub-sub stop-channel-based-pub-sub]]
            [mercurius.core.adapters.processes.activity-logger :refer [new-activity-logger]]
            [mercurius.core.adapters.controllers.request-processor :refer [new-request-processor]]
            [mercurius.core.adapters.controllers.event-notifier :refer [start-event-notifier]]
            [mercurius.core.infraestructure.web.server :refer [start-web-server stop-web-server]]
            [mercurius.core.infraestructure.web.sente :refer [start-sente stop-sente]]
            [mercurius.accounts.domain.use-cases.authenticate :refer [new-authenticate-use-case]]
            [mercurius.accounts.adapters.repositories.in-memory-user-repository :refer [new-in-memory-user-repo]]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [new-in-memory-wallet-repo]]
            [mercurius.wallets.adapters.presenters.wallet-presenter :refer [wallet-edn-presenter]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [load-wallet save-wallet get-user-wallets calculate-monetary-base]]
            [mercurius.wallets.domain.use-cases.deposit :refer [new-deposit-use-case]]
            [mercurius.wallets.domain.use-cases.withdraw :refer [new-withdraw-use-case]]
            [mercurius.wallets.domain.use-cases.transfer :refer [new-transfer-use-case]]
            [mercurius.wallets.domain.use-cases.get-wallet :refer [new-get-wallet-use-case]]
            [mercurius.wallets.domain.use-cases.get-wallets :refer [new-get-wallets-use-case]]
            [mercurius.wallets.domain.use-cases.calculate-monetary-base :refer [new-calculate-monetary-base-use-case]]
            [mercurius.trading.adapters.repositories.in-memory-order-book-repository :refer [new-in-memory-order-book-repo]]
            [mercurius.trading.adapters.repositories.in-memory-ticker-repository :refer [new-in-memory-ticker-repo]]
            [mercurius.trading.adapters.repositories.in-memory-trade-repository :refer [new-in-memory-trade-repo]]
            [mercurius.trading.adapters.processes.trade-finder :refer [new-trade-finder]]
            [mercurius.trading.adapters.processes.trade-processor :refer [new-trade-processor]]
            [mercurius.trading.domain.repositories.order-book-repository :refer [insert-order update-order remove-order get-bids-asks get-bid-ask get-order-book]]
            [mercurius.trading.domain.repositories.ticker-repository :refer [update-ticker get-tickers get-ticker]]
            [mercurius.trading.domain.repositories.trade-repository :refer [add-trade get-trades]]
            [mercurius.trading.domain.use-cases.place-order :refer [new-place-order-use-case]]
            [mercurius.trading.domain.use-cases.get-order-book :refer [new-get-order-book-use-case]]
            [mercurius.trading.domain.use-cases.execute-trades :refer [new-execute-trades-use-case]]
            [mercurius.trading.domain.use-cases.process-trade :refer [new-process-trade-use-case]]
            [mercurius.trading.domain.use-cases.get-tickers :refer [new-get-tickers-use-case]]
            [mercurius.trading.domain.use-cases.get-trades :refer [new-get-trades-use-case]]))

(defn build-assembly [{:keys [port session-key]}]
  {:infraestructure/pub-sub nil
   :adapters/wallet-repo nil
   :adapters/order-book-repo nil
   :adapters/ticker-repo nil
   :adapters/trade-repo nil
   :adapters/user-repo nil
   :adapters/event-bus {:pub-sub (ig/ref :infraestructure/pub-sub)}
   :use-cases/authenticate {:user-repo (ig/ref :adapters/user-repo)}
   :use-cases/deposit {:wallet-repo (ig/ref :adapters/wallet-repo)
                       :event-bus (ig/ref :adapters/event-bus)}
   :use-cases/withdraw {:wallet-repo (ig/ref :adapters/wallet-repo)
                        :event-bus (ig/ref :adapters/event-bus)}
   :use-cases/transfer {:wallet-repo (ig/ref :adapters/wallet-repo)
                        :event-bus (ig/ref :adapters/event-bus)}
   :use-cases/get-wallet {:wallet-repo (ig/ref :adapters/wallet-repo)}
   :use-cases/get-wallets {:wallet-repo (ig/ref :adapters/wallet-repo)
                           :presenter wallet-edn-presenter}
   :use-cases/calculate-monetary-base {:wallet-repo (ig/ref :adapters/wallet-repo)}
   :use-cases/place-order {:wallet-repo (ig/ref :adapters/wallet-repo)
                           :order-book-repo (ig/ref :adapters/order-book-repo)
                           :event-bus (ig/ref :adapters/event-bus)}
   :use-cases/get-order-book {:order-book-repo (ig/ref :adapters/order-book-repo)}
   :use-cases/execute-trades {:order-book-repo (ig/ref :adapters/order-book-repo)
                              :transfer-use-case (ig/ref :use-cases/transfer)
                              :event-bus (ig/ref :adapters/event-bus)}
   :use-cases/process-trade {:ticker-repo (ig/ref :adapters/ticker-repo)
                             :trades-repo (ig/ref :adapters/trade-repo)
                             :event-bus (ig/ref :adapters/event-bus)}
   :use-cases/get-tickers {:ticker-repo (ig/ref :adapters/ticker-repo)}
   :use-cases/get-trades {:trades-repo (ig/ref :adapters/trade-repo)}
   :use-cases/dispatch {:handlers {:authenticate (ig/ref :use-cases/authenticate)
                                   :deposit (ig/ref :use-cases/deposit)
                                   :withdraw (ig/ref :use-cases/withdraw)
                                   :transfer (ig/ref :use-cases/transfer)
                                   :get-wallet (ig/ref :use-cases/get-wallet)
                                   :get-wallets (ig/ref :use-cases/get-wallets)
                                   :calculate-monetary-base (ig/ref :use-cases/calculate-monetary-base)
                                   :place-order (ig/ref :use-cases/place-order)
                                   :get-order-book (ig/ref :use-cases/get-order-book)
                                   :execute-trades (ig/ref :use-cases/execute-trades)
                                   :process-trade (ig/ref :use-cases/process-trade)
                                   :get-tickers (ig/ref :use-cases/get-tickers)
                                   :get-trades (ig/ref :use-cases/get-trades)}
                        :middleware [logger stm]}
   :processes/trade-finder {:event-bus (ig/ref :adapters/event-bus)
                            :dispatch (ig/ref :use-cases/dispatch)}
   :processes/trade-processor {:event-bus (ig/ref :adapters/event-bus)
                               :dispatch (ig/ref :use-cases/dispatch)}
   :processes/activity-logger {:event-bus (ig/ref :adapters/event-bus)}
   :controllers/request-processor {:dispatch (ig/ref :use-cases/dispatch)}
   :controllers/event-notifier {:event-bus (ig/ref :adapters/event-bus)
                                :pub-sub (ig/ref :infraestructure/pub-sub)}
   :infraestructure/web-server {:port port
                                :session-key session-key
                                :sente (ig/ref :infraestructure/sente)
                                :dispatch (ig/ref :use-cases/dispatch)}
   :infraestructure/sente {:request-processor (ig/ref :controllers/request-processor)
                           :pub-sub (ig/ref :infraestructure/pub-sub)}})

(defmethod ig/init-key :adapters/wallet-repo [_ _]
  (new-in-memory-wallet-repo))

(defmethod ig/init-key :adapters/order-book-repo [_ _]
  (new-in-memory-order-book-repo))

(defmethod ig/init-key :adapters/ticker-repo [_ _]
  (new-in-memory-ticker-repo))

(defmethod ig/init-key :adapters/trade-repo [_ _]
  (new-in-memory-trade-repo))

(defmethod ig/init-key :adapters/user-repo [_ _]
  (new-in-memory-user-repo))

(defmethod ig/init-key :infraestructure/pub-sub [_ _]
  (start-channel-based-pub-sub))

(defmethod ig/halt-key! :infraestructure/pub-sub [_ pub-sub]
  (stop-channel-based-pub-sub pub-sub))

(defmethod ig/init-key :adapters/event-bus [_ deps]
  (new-pub-sub-event-bus deps))

(defmethod ig/init-key :use-cases/authenticate [_ deps]
  (new-authenticate-use-case deps))

(defmethod ig/init-key :use-cases/deposit [_ {:keys [wallet-repo event-bus]}]
  (new-deposit-use-case {:load-wallet (partial load-wallet wallet-repo)
                         :save-wallet (partial save-wallet wallet-repo)
                         :publish-events (partial emit event-bus)}))

(defmethod ig/init-key :use-cases/withdraw [_ {:keys [wallet-repo event-bus]}]
  (new-withdraw-use-case {:load-wallet (partial load-wallet wallet-repo)
                          :save-wallet (partial save-wallet wallet-repo)
                          :publish-events (partial emit event-bus)}))

(defmethod ig/init-key :use-cases/transfer [_ {:keys [wallet-repo event-bus]}]
  (new-transfer-use-case {:load-wallet (partial load-wallet wallet-repo)
                          :save-wallet (partial save-wallet wallet-repo)
                          :publish-events (partial emit event-bus)}))

(defmethod ig/init-key :use-cases/get-wallet [_ {:keys [wallet-repo]}]
  (new-get-wallet-use-case {:load-wallet (partial load-wallet wallet-repo)}))

(defmethod ig/init-key :use-cases/get-wallets [_ {:keys [wallet-repo presenter]}]
  (new-get-wallets-use-case {:get-user-wallets (partial get-user-wallets wallet-repo)
                             :presenter presenter}))

(defmethod ig/init-key :use-cases/calculate-monetary-base [_ {:keys [wallet-repo]}]
  (new-calculate-monetary-base-use-case
   {:calculate-monetary-base (partial calculate-monetary-base wallet-repo)}))

(defmethod ig/init-key :use-cases/place-order [_ {:keys [wallet-repo order-book-repo event-bus]}]
  (new-place-order-use-case {:load-wallet (partial load-wallet wallet-repo)
                             :save-wallet (partial save-wallet wallet-repo)
                             :get-bid-ask (partial get-bid-ask order-book-repo)
                             :insert-order (partial insert-order order-book-repo)
                             :publish-events (partial emit event-bus)}))

(defmethod ig/init-key :use-cases/get-order-book [_ {:keys [order-book-repo]}]
  (new-get-order-book-use-case {:get-order-book (partial get-order-book order-book-repo)}))

(defmethod ig/init-key :use-cases/execute-trades [_ {:keys [order-book-repo transfer-use-case event-bus]}]
  (new-execute-trades-use-case {:get-bids-asks (partial get-bids-asks order-book-repo)
                                :update-order (partial update-order order-book-repo)
                                :remove-order (partial remove-order order-book-repo)
                                :transfer transfer-use-case
                                :publish-event (partial emit event-bus)}))

(defmethod ig/init-key :use-cases/process-trade [_ {:keys [trades-repo ticker-repo event-bus]}]
  (new-process-trade-use-case {:add-trade (partial add-trade trades-repo)
                               :get-ticker (partial get-ticker ticker-repo)
                               :update-ticker (partial update-ticker ticker-repo)
                               :publish-events (partial emit event-bus)}))

(defmethod ig/init-key :use-cases/get-tickers [_ {:keys [ticker-repo]}]
  (new-get-tickers-use-case {:get-tickers (partial get-tickers ticker-repo)}))

(defmethod ig/init-key :use-cases/get-trades [_ {:keys [trades-repo]}]
  (new-get-trades-use-case {:get-trades (partial get-trades trades-repo)}))

(defmethod ig/init-key :use-cases/dispatch [_ {:keys [handlers middleware]}]
  (let [mediator (new-mediator handlers middleware)]
    (partial dispatch mediator)))

(defmethod ig/init-key :processes/trade-finder [_ {:keys [event-bus dispatch]}]
  (new-trade-finder {:event-bus event-bus
                     :execute-trades (partial dispatch :execute-trades)}))

(defmethod ig/init-key :processes/trade-processor [_ {:keys [event-bus dispatch]}]
  (new-trade-processor {:event-bus event-bus
                        :process-trade (partial dispatch :process-trade)}))

(defmethod ig/init-key :processes/activity-logger [_ {:keys [event-bus]}]
  (new-activity-logger {:event-bus event-bus}))

(defmethod ig/init-key :infraestructure/web-server [_ deps]
  (start-web-server deps))

(defmethod ig/halt-key! :infraestructure/web-server [_ server]
  (stop-web-server server))

(defmethod ig/init-key :controllers/request-processor [_ deps]
  (new-request-processor deps))

(defmethod ig/init-key :controllers/event-notifier [_ deps]
  (start-event-notifier deps))

(defmethod ig/init-key :infraestructure/sente [_ deps]
  (start-sente deps))

(defmethod ig/halt-key! :infraestructure/sente [_ sente]
  (stop-sente sente))

(defn start
  "Injects all the dependencies into the respective components and starts the system.
  Returns a map containing all the components."
  [& [{:keys [only]}]]
  (let [config (read-config)
        assembly (build-assembly config)]
    (configure-logger config)
    (log/info "Starting system with config" (pr-str config))
    (try
      (if only
        (ig/init assembly only)
        (ig/init assembly))
      (catch clojure.lang.ExceptionInfo ex
        (ig/halt! (:system (ex-data ex)))
        (throw (.getCause ex))))))

(defn stop [system & [{:keys [only]}]]
  (when system
    (log/info "Stopping system")
    (if only
      (ig/halt! system only)
      (ig/halt! system)))
  nil)

(comment
  (def system (start {:only [:use-cases/dispatch]}))
  (stop system))
