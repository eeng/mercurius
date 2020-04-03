(ns fiddles.sente
  (:require [user :refer [system]]
            [mercurius.core.adapters.web.sente :refer [broadcast!]]))

(comment
  (def sente (:adapters/sente system))
  (broadcast! sente [:trading/ticker-updated {:ticker "BTCUSD" :last-price 100M :volume 900M}]))
