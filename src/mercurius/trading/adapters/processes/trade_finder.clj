(ns mercurius.trading.adapters.processes.trade-finder
  (:require [clojure.core.async :refer [thread]]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]
            [taoensso.timbre :as log]))

(defn- process-bids-asks [{:keys [active execute-trades run-every-ms tickers] :as tf}]
  (doseq [ticker tickers]
    (thread
      (Thread/sleep run-every-ms)
      (when @active
        (try
          (execute-trades {:ticker ticker})
          (catch Exception e
            (log/error e)
            (throw e)))
        (recur))))
  tf)

(defrecord TradeFinder [active execute-trades run-every-ms tickers]
  java.io.Closeable
  (close [this]
    (log/info "Stopping trade finder")
    (reset! active false)
    this))

(defn start-trade-finder
  "Starts a background job that, for each ticker, matches bids and asks to discover trades."
  [{:keys [execute-trades run-every-ms] :or {run-every-ms 1000}}]
  (log/info "Starting trade finder")
  (-> (TradeFinder. (atom (pos? run-every-ms)) execute-trades run-every-ms available-tickers)
      (process-bids-asks)))

(comment
  (def tf (start-trade-finder {:execute-trades #(log/info "Matching" %)}))
  (.close tf))
