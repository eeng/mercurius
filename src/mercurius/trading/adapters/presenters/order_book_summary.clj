(ns mercurius.trading.adapters.presenters.order-book-summary
  (:require [clojure.spec.alpha :as s]
            [mercurius.util.collections :refer [sum-by]]))

(defn- min-divisor-for-price [price]
  (->> price Math/log10 Math/ceil dec (Math/pow 10)))

(def precisions-to-exponent {"P0" 4 "P1" 3 "P2" 2 "P3" 1 "P4" 0})

(defn precision-to-pow-of-ten [precision max-price]
  (s/assert ::precision precision)
  (let [min-divisor (min-divisor-for-price max-price)
        exponent (precisions-to-exponent precision)]
    (/ min-divisor (.pow 10M exponent))))

(s/def ::precision (set (keys precisions-to-exponent)))

(defn- find-max-price [{:keys [buying selling]}]
  (max (-> buying first :price (or 0))
       (-> selling first :price (or 0))))

(defn round-with-pow-of-ten [number divisor]
  (- number (mod number divisor)))

(defn- build-summary-at-price [[price orders]]
  {:price price
   :count (count orders)
   :amount (sum-by :remaining orders)})

(defn- summarize [orders pow-of-ten order limit]
  (let [price-cmp (case order
                    :asc #(compare %1 %2)
                    :desc #(compare %2 %1))]
    (->> orders
         (group-by #(round-with-pow-of-ten (:price %) pow-of-ten))
         (map build-summary-at-price)
         (sort-by :price price-cmp)
         (take (or limit 100)))))

(defn summarize-order-book [order-book {:keys [precision limit]}]
  (let [max-price (find-max-price order-book)
        pow-of-ten (precision-to-pow-of-ten precision max-price)]
    (-> order-book
        (update :buying summarize pow-of-ten :desc limit)
        (update :selling summarize pow-of-ten :asc limit))))
