(ns mercurius.trading.domain.repositories.order-book-repository)

(defprotocol OrderBookRepository
  (insert-order [this order]
    "Inserts the order in the order book."))
