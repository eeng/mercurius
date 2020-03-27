(ns mercurius.trading.adapters.processes.trade-finder-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.core.async :refer [>!! chan timeout alts!!]]
            [matcher-combinators.test]
            [matcher-combinators.matchers :as m]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]
            [mercurius.trading.adapters.processes.trade-finder :refer [start-trade-finder]]))

(deftest trade-finder-test
  (testing "runs a thread for each ticker that provides the bid and ask to the match orders use case"
    (let [calls (chan)]
      (with-open [_ (start-trade-finder
                     {:execute-trades (fn [args] (>!! calls args))
                      :run-every-ms 1})]
        (is (match? (m/in-any-order available-tickers)
                    (for [_ available-tickers]
                      (-> (alts!! [calls (timeout 100)])
                          first :ticker))))))))
