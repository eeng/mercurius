(ns mercurius.trading.adapters.presenters.order-book-summary
  (:require [clojure.spec.alpha :as s]
            [mercurius.util.collections :refer [sum-by]]
            [mercurius.util.number :refer [round-to-decimal-places]]))

(def precision-translations {"P0" 0 "P1" 1 "P2" 2 "P3" 3 "P4" 4})

(defn precision-to-decimal-places [precision max-price]
  (s/assert ::precision precision)
  (when (pos? max-price)
    (let [n (->> max-price Math/log10 Math/floor int inc)]
      (- (count precision-translations)
         (precision-translations precision)
         n))))

(s/def ::precision (set (keys precision-translations)))

(defn- find-max-price [{:keys [buying selling]}]
  (max (-> buying first :price (or 0))
       (-> selling first :price (or 0))))

(defn round-with-pow-of-ten [number pow-of-ten]
  (- number (mod number pow-of-ten)))

(defn- build-summary-at-price [[price orders]]
  {:price price
   :count (count orders)
   :amount (sum-by :remaining orders)})

(defn- summarize [orders decimal-places order limit]
  (let [price-cmp (case order
                    :asc #(compare %1 %2)
                    :desc #(compare %2 %1))]
    (->> orders
         (group-by #(round-to-decimal-places (:price %) decimal-places))
         (map build-summary-at-price)
         (sort-by :price price-cmp)
         (take (or limit 100)))))

(defn summarize-order-book [order-book {:keys [precision limit]}]
  (let [max-price (find-max-price order-book)
        decimal-places (precision-to-decimal-places precision max-price)]
    (-> order-book
        (update :buying summarize decimal-places :desc limit)
        (update :selling summarize decimal-places :asc limit))))
