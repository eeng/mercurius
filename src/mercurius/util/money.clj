(ns mercurius.util.money
  (:require [mercurius.util.number :refer [round-to-decimal-places round-to-significant-figures]]))

(defn money [amount & {:keys [precision] :or {precision 8}}]
  (-> amount (round-to-decimal-places precision) bigdec))

(defn money-sf [amount]
  (-> amount (round-to-significant-figures 5) bigdec))
