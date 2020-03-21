(ns mercurius.trading.domain.repositories.order-book-repository)

(defprotocol OrderBookRepository
  (insert-order [this order]
    "Inserts the order in the order book.")

  (get-order-book [this ticker]
    "Returns the full order book as a map with :buying and :selling keys to orders.")

  (get-bids-asks [this ticker]
    "Returns the bids (orders at the best buying price) and asks (orders at the best selling price)."))
