(ns mercurius.trading.adapters.processes.bid-ask-provider
  (:require [clojure.core.async :refer [thread]]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]
            [taoensso.timbre :as log]))

(defn- process-bids-asks [{:keys [active get-bid-ask match-orders run-every-ms tickers] :as bap}]
  (doseq [ticker tickers]
    (thread
      (try
        (loop [last-bid-ask-processed nil]
          (Thread/sleep run-every-ms)
          (when @active
            (let [{:keys [bid ask] :as bid-ask} (get-bid-ask ticker)]
              (when (and bid ask (not= bid-ask last-bid-ask-processed))
                (match-orders bid-ask))
              (recur bid-ask))))
        (catch Exception e
          (log/error e)
          (throw e)))))
  bap)

(defrecord BidAskProvider [active get-bid-ask match-orders run-every-ms tickers])

(defn start-bid-ask-provider
  "Starts a background job for each ticker in search of possible trades.
  Calls the match-orders use case when a bid and ask is return."
  [{:keys [get-bid-ask match-orders run-every-ms] :or {run-every-ms 1000}}]
  (-> (BidAskProvider. (atom true) get-bid-ask match-orders run-every-ms available-tickers)
      (process-bids-asks)))

(defn stop-bid-ask-provider [{:keys [active] :as bap}]
  (reset! active false)
  bap)

(comment
  (def bap (start-bid-ask-provider
            {:get-bid-ask (fn [ticker] {:bid (str "bid " (rand-int 5)) :ask (str "ask " ticker)})
             :match-orders #(log/info "Matching" %)}))
  (stop-bid-ask-provider bap))
