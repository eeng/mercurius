(ns mercurius.trading.adapters.repositories.in-memory-ticker-repository
  (:require [mercurius.trading.domain.repositories.ticker-repository :refer [TickerRepository]]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]))

(defrecord InMemoryTickerRepository [db]
  TickerRepository

  (update-ticker [_ {:keys [ticker price amount]}]
    (swap! db #(-> %
                   (assoc-in [ticker :last-price] price)
                   (update-in [ticker :volume] + amount)))
    (@db ticker))

  (get-tickers [_]
    @db))

(defn new-in-memory-ticker-repo []
  (InMemoryTickerRepository.
   (atom (->> available-tickers
              (map (fn [t] [t {:ticker t :last-price 0 :volume 0M}]))
              (into {})))))
