(ns mercurius.trading.domain.use-cases.get-trades)

(defn new-get-trades-use-case
  "Returns the trades for a ticker."
  [{:keys [get-trades]}]
  (fn [{:keys [ticker]}]
    (get-trades ticker)))
