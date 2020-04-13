(ns mercurius.trading.adapters.repositories.in-memory-trade-repository
  (:require [mercurius.trading.domain.repositories.trade-repository :refer [TradeRepository add-trade get-trades]]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]))

(defrecord InMemoryTradeRepository [db]
  TradeRepository

  (add-trade [_ {:keys [ticker] :as trade}]
    (swap! db update ticker conj (dissoc trade :bid :ask)))

  (get-trades [_ ticker]
    (get @db ticker [])))

(defn new-in-memory-trades-repo []
  (InMemoryTradeRepository.
   (atom (zipmap available-tickers
                 (repeat (count available-tickers) '())))))

(comment
  (def repo (new-in-memory-trades-repo))
  (add-trade repo {:ticker "BTCUSD" :amount 10 :price 200})
  (add-trade repo {:ticker "BTCUSD" :amount 11 :price 200})
  (get-trades repo "BTCUSD"))
