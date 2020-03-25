(ns mercurius.trading.domain.repositories.order-book-repository)

(defprotocol OrderBookRepository
  (insert-order [this order]
    "Inserts the order in the order book.")

  (update-order [this order]
    "Updates the order by id.")

  (remove-order [this order]
    "Removes the order by id.")

  (get-order-book [this ticker]
    "Returns the full order book as a map with :buying and :selling keys to orders.")

  (get-bids-asks [this ticker]
    "Returns the bids and asks orders that will be matched for trades.
    Bids are those orders with price greater or equal to the best selling price.
    Conversely, asks are those with price lower or equal to the best buying price."))
