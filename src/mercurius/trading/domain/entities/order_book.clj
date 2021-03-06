(ns mercurius.trading.domain.entities.order-book)

(defn new-order-book []
  {:buying [] :selling []})

(defn sort-orders-for-side [side orders]
  (let [price-cmp (case side
                    :buying #(- (:price %))
                    :selling :price)]
    (sort-by (juxt price-cmp :placed-at) orders)))
