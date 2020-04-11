(ns mercurius.trading.domain.repositories.trades-repository)

(defprotocol TradesRepository
  (add-trade [this trade]
    "Adds the trade to the repository.")

  (get-trades [this ticker]
    "Returns all the trades for the `ticker`."))

(defn adapt-for-storage [trade {:keys [id]}]
  (-> trade
      (dissoc :bid :ask)
      (assoc :id id)))
