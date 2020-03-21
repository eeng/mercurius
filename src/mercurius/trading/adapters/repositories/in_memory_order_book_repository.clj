(ns mercurius.trading.adapters.repositories.in-memory-order-book-repository
  (:require [mercurius.trading.domain.repositories.order-book-repository :refer [OrderBookRepository get-order-book]]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]
            [mercurius.util.collections :refer [update-where]]))

(defn- find-best-orders [repo ticker side]
  (let [all-orders (side (get-order-book repo ticker))
        best-price (-> all-orders first :price)]
    (filter #(= (:price %) best-price) all-orders)))

(defn- book-side [{:keys [side]}]
  (case side :buy :buying :sell :selling))

(defn- replace-order [orders {:keys [id] :as order}]
  (update-where orders #(= (:id %) id) (constantly order)))

(defn- remove-order [orders {:keys [id]}]
  (remove #(= (:id %) id) orders))

(defrecord InMemoryOrderBookRepository [db]
  OrderBookRepository

  (insert-order [_ {:keys [ticker] :as order}]
    (swap! db update-in [ticker (book-side order)] conj order)
    order)

  (update-order [_ {:keys [ticker id] :as order}]
    (swap! db update-in [ticker (book-side order)] replace-order order)
    order)

  (remove-order [_ {:keys [ticker id] :as order}]
    (swap! db update-in [ticker (book-side order)] remove-order order)
    order)

  (get-order-book [_ ticker]
    (let [buying-sorter (comp reverse (partial sort-by :price))
          selling-sorter (partial sort-by :price)]
      (-> (get @db ticker)
          (update :buying buying-sorter)
          (update :selling selling-sorter))))

  (get-bids-asks [this ticker]
    {:bids (find-best-orders this ticker :buying)
     :asks (find-best-orders this ticker :selling)}))

(defn new-in-memory-order-book-repo []
  (InMemoryOrderBookRepository.
   (atom (zipmap available-tickers
                 (repeat (count available-tickers) {:buying [] :selling []})))))
