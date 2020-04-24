(ns mercurius.core.presentation.util.format
  (:require [goog.string :as gstring]
            [tick.locale-en-us]
            [tick.alpha.api :as t]))

(defn format-number [num]
  (gstring/format "%.2f" num))

(defn format-time [date]
  (gstring/format "%02d:%02d:%02d" (t/hour date) (t/minute date) (t/second date)))
