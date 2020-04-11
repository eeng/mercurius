(ns mercurius.core.presentation.util.format
  (:require [goog.string :as gstring]
            [tick.locale-en-us]
            [tick.alpha.api :as t]))

(defn format-money [num]
  (gstring/format "$%.2f" num))

(defn format-time [date]
  (gstring/format "%2d:%2d:%2d" (t/hour date) (t/minute date) (t/second date)))
