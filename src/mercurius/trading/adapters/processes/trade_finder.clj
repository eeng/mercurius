(ns mercurius.trading.adapters.processes.trade-finder
  (:require [mercurius.trading.domain.entities.ticker :refer [available-tickers]]
            [mercurius.core.domain.messaging.event-bus :refer [listen]]
            [taoensso.timbre :as log]))

(defn new-trade-finder
  "Starts a background job that, for each ticker, matches bids and asks to discover trades."
  [{:keys [event-bus execute-trades]}]
  (doseq [ticker available-tickers]
    (log/info "Starting trade finder for" ticker)
    (listen event-bus
            :order-placed
            (fn [{order :data}]
              (when (= ticker (:ticker order))
                (execute-trades {:ticker ticker}))))))
