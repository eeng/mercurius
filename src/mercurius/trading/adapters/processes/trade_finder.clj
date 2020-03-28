(ns mercurius.trading.adapters.processes.trade-finder
  (:require [clojure.core.async :refer [<! go-loop sliding-buffer chan]]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]
            [taoensso.timbre :as log]))

(defn new-trade-finder
  "Starts a background job that, for each ticker, matches bids and asks to discover trades."
  [{:keys [subscribe-to execute-trades]}]
  (doseq [ticker available-tickers]
    (log/info "Starting trade finder for" ticker)
    (let [events (subscribe-to :order-placed :out-chan (chan (sliding-buffer 1)))]
      (go-loop []
        (if-let [{order :data} (<! events)]
          (do
            (when (= ticker (:ticker order))
              (log/debug "Finding trades for" ticker)
              (execute-trades {:ticker ticker}))
            (recur))
          (log/info "Stopping trade finder" ticker))))))
