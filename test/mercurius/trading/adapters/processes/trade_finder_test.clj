(ns mercurius.trading.adapters.processes.trade-finder-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.core.async :refer [>!! chan timeout alts!! to-chan]]
            [matcher-combinators.test]
            [matcher-combinators.matchers :as m]
            [mercurius.support.factory :refer [build-order]]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]
            [mercurius.trading.adapters.processes.trade-finder :refer [new-trade-finder]]))

(deftest trade-finder-test
  (testing "runs a process for each ticker that calls the corresponding execute-trades use case"
    (let [calls (chan)
          one-order-for-ticker-chan #(to-chan [{:data (build-order {:ticker %})}])
          events-channs (atom (map one-order-for-ticker-chan available-tickers))]
      (new-trade-finder
       {:subscribe #(let [[chan & rest] @events-channs]
                      (is (= :order-placed %))
                      (reset! events-channs rest)
                      chan)
        :execute-trades (fn [args] (>!! calls args))})
      (is (match? (m/in-any-order available-tickers)
                  (for [_ available-tickers]
                    (-> (alts!! [calls (timeout 100)])
                        first :ticker)))))))
