(ns mercurius.trading.adapters.processes.trade-finder
  (:require [clojure.core.async :refer [<! go-loop]]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]
            [taoensso.timbre :as log]))

(defn new-trade-finder
  "Starts a background job that, for each ticker, matches bids and asks to discover trades."
  [{:keys [subscribe execute-trades]}]
  (doseq [ticker available-tickers]
    (log/info "Starting trade finder for" ticker)
    (let [events (subscribe :order-placed)]
      (go-loop []
        (if-let [{order :data} (<! events)]
          (do
            (when (= ticker (:ticker order))
              (log/info "Finding trades for" ticker)
              (execute-trades {:ticker ticker}))
            (recur))
          (log/info "Stopping trade finder" ticker))))))
