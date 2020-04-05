(ns fiddles.pub-sub
  (:require [user :refer [system]]
            [mercurius.core.adapters.controllers.pub-sub :refer [broadcast!]]))

(comment
  (def pub-sub (:adapters/sente system))
  (broadcast! pub-sub [:trading/ticker-updated {:ticker "BTCUSD" :last-price 100M :volume 900M}]))
