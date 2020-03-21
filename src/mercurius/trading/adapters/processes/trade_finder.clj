(ns mercurius.trading.adapters.processes.trade-finder
  (:require [clojure.core.async :refer [thread]]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]
            [taoensso.timbre :as log]))

(defn- process-bids-asks [{:keys [active execute-trades run-every-ms tickers] :as bap}]
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
  bap)

(defrecord BidAskProvider [active execute-trades run-every-ms tickers])

(defn start-trade-finder
  "Starts a background job that, for each ticker, matches bids and asks to discover trades."
  [{:keys [execute-trades run-every-ms] :or {run-every-ms 1000}}]
  (-> (BidAskProvider. (atom true) execute-trades run-every-ms available-tickers)
      (process-bids-asks)))

(defn stop-trade-finder [{:keys [active] :as bap}]
  (reset! active false)
  bap)

(comment
  (def bap (start-trade-finder
            {:get-bids-asks (fn [ticker] {:bids [(str "bid " (rand-int 5))] :asks [(str "ask " ticker)]})
             :execute-trades #(log/info "Matching" %)}))
  (stop-trade-finder bap))
