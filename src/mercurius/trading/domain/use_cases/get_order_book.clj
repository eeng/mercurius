(ns mercurius.trading.domain.use-cases.get-order-book
  (:require [clojure.spec.alpha :as s]
            [mercurius.accounts.domain.entities.user]
            [mercurius.trading.domain.entities.ticker :as ticker]
            [mercurius.trading.adapters.presenters.order-book-summary :as summary :refer [summarize-order-book]]))

(s/def ::ticker ::ticker/ticker)
(s/def ::precision (s/or :summarized ::summary/precision
                         :raw #{"R0"}))
(s/def ::command (s/keys :req-un [::ticker] :opt-un [::precision]))

(defn new-get-order-book-use-case
  "Returns a use case that provides access to the order book of a ticker."
  [{:keys [get-order-book]}]
  (fn [{:keys [ticker precision limit] :as command :or {precision "R0" limit 100}}]
    (s/assert ::command command)
    (let [book (get-order-book ticker)]
      (if (= precision "R0")
        (-> book
            (update :buying (partial take limit))
            (update :selling (partial take limit)))
        (summarize-order-book book {:precision precision :limit limit})))))
