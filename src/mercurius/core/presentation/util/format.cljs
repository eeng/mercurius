(ns mercurius.core.presentation.util.format
  (:require [goog.string :as gstring]))

(defn format-money [num]
  (gstring/format "$%.2f" num))
