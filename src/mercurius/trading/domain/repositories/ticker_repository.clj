(ns mercurius.trading.domain.repositories.ticker-repository)

(defprotocol TickerRepository
  (update-ticker [this last-trade]
    "Updates the ticker current price and volume.
    Returns the updated ticker data.")

  (get-tickers [this]
    "Returns a map of all tickers and its stats (current price, volume, etc.)"))

(defn get-ticker [repo ticker]
  (-> (get-tickers repo)
      (get ticker)))
