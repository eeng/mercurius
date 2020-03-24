(ns mercurius.wallets.domain.use-cases.calculate-monetary-base)

(defn new-calculate-monetary-base-use-case
  "Returns a use case that can calculate the total monetary base of the system."
  [{:keys [calculate-monetary-base]}]
  (fn [_]
    (calculate-monetary-base)))
