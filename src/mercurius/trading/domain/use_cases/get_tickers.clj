(ns mercurius.trading.domain.use-cases.get-tickers)

(defn new-get-tickers-use-case
  "Returns the tickers stats."
  [{:keys [get-tickers]}]
  (fn [_]
    (get-tickers)))
