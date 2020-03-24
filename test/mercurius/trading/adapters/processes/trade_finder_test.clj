(ns mercurius.trading.adapters.processes.trade-finder-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.core.async :refer [>!! chan timeout alts!!]]
            [matcher-combinators.test]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]
            [mercurius.trading.adapters.processes.trade-finder :refer [start-trade-finder stop-trade-finder]]))

(deftest trade-finder-test
  (testing "runs a thread for each ticker that provides the bid and ask to the match orders use case"
    (let [calls (chan)
          tf (start-trade-finder
              {:execute-trades (fn [ticker] (>!! calls ticker))
               :run-every-ms 1})
          [calls-val _] (alts!! [calls (timeout 1000)])]
      (is (contains? (set available-tickers) (:ticker calls-val)))
      (stop-trade-finder tf))))
