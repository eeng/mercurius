(ns mercurius.trading.adapters.repositories.in-memory-order-book-repository
  (:require [mercurius.trading.domain.repositories.order-book-repository :refer [OrderBookRepository]]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]))

(defrecord InMemoryOrderBookRepository [db]
  OrderBookRepository

  (insert-order [_ {:keys [ticker side] :as order}]
    (let [side (case side :buy :buying :sell :selling)]
      (swap! db update-in [ticker side] conj order)))

  (get-order-book [_ ticker]
    (let [buying-sorter (comp reverse (partial sort-by :price))
          selling-sorter (partial sort-by :price)]
      (-> (get @db ticker)
          (update :buying buying-sorter)
          (update :selling selling-sorter)))))

(defn new-in-memory-order-book-repo []
  (InMemoryOrderBookRepository.
   (atom (zipmap available-tickers
                 (repeat (count available-tickers) {:buying [] :selling []})))))
