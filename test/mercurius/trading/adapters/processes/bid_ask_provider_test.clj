(ns mercurius.trading.adapters.processes.bid-ask-provider-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.core.async :refer [>!! chan timeout alts!!]]
            [matcher-combinators.test]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]
            [mercurius.trading.adapters.processes.bid-ask-provider :refer [start-bid-ask-provider stop-bid-ask-provider]]))

(deftest bid-ask-provider-test
  (testing "runs a thread for each ticker that provides the bid and ask to the match orders use case"
    (let [calls (chan)
          bap (start-bid-ask-provider
               {:get-bid-ask (fn [ticker] {:bid ticker :ask ticker})
                :match-orders (fn [{:keys [bid]}] (>!! calls bid))
                :run-every-ms 0})]
      (let [[calls-val _] (alts!! [calls (timeout 1000)])]
        (is (contains? (set available-tickers) calls-val)))
      (stop-bid-ask-provider bap))))
