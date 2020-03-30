(ns mercurius.trading.adapters.repositories.in-memory-order-book-repository
  (:require [mercurius.trading.domain.repositories.order-book-repository :refer [OrderBookRepository get-order-book]]
            [mercurius.trading.domain.entities.ticker :refer [available-tickers]]
            [mercurius.trading.domain.entities.order-book :refer [new-order-book sort-orders-for-side]]))

(defn- take-orders-surpassing [book side threshold]
  (let [comparator (case side :buying >= :selling <=)]
    (if threshold
      (->> book side (take-while #(comparator (:price %) threshold)))
      [])))

(defn- book-side [{:keys [side]}]
  (case side :buy :buying :sell :selling))

(defn- replace-order [orders {:keys [id] :as changed-order}]
  (map #(if (= (:id %) id) changed-order %) orders))

(defn- remove-order [orders {:keys [id]}]
  (remove #(= (:id %) id) orders))

(defrecord InMemoryOrderBookRepository [db]
  OrderBookRepository

  (insert-order [_ {:keys [ticker] :as order}]
    (alter db update-in [ticker (book-side order)] conj order)
    order)

  (update-order [_ {:keys [ticker id] :as order}]
    (alter db update-in [ticker (book-side order)] replace-order order)
    order)

  (remove-order [_ {:keys [ticker id] :as order}]
    (alter db update-in [ticker (book-side order)] remove-order order)
    order)

  (get-order-book [_ ticker]
    (-> (get @db ticker)
        (update :buying (partial sort-orders-for-side :buying))
        (update :selling (partial sort-orders-for-side :selling))))

  (get-bids-asks [this ticker]
    (let [book (get-order-book this ticker)
          bid (->> book :buying first :price)
          ask (->> book :selling first :price)]
      {:bids (take-orders-surpassing book :buying ask)
       :asks (take-orders-surpassing book :selling bid)})))

(defn new-in-memory-order-book-repo []
  (InMemoryOrderBookRepository.
   (ref (zipmap available-tickers
                (repeat (count available-tickers) (new-order-book))))))
