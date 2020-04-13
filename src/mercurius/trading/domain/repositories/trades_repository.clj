(ns mercurius.trading.domain.repositories.trades-repository)

(defprotocol TradesRepository
  (add-trade [this trade]
    "Adds the trade to the repository.")

  (get-trades [this ticker]
    "Returns all the trades for the `ticker`, most recent first."))

(defn adapt-for-storage [trade {:keys [id created-at]}]
  (-> trade
      (dissoc :bid :ask)
      (assoc :id id :created-at created-at)))
