(ns mercurius.simulation.adapters.processes.simulator
  (:require [clojure.core.async :refer [thread]]
            [taoensso.timbre :as log]
            [mercurius.util.progress :refer [new-progress-tracker finish!]]
            [mercurius.core.adapters.messaging.pub-sub :refer [publish]]
            [mercurius.simulation.adapters.processes.simulation :refer [run-simulation]]))

(defrecord Simulator [running dispatch pub-sub])

(defn new-simulator [{:keys [dispatch pub-sub]}]
  (Simulator. (atom false) dispatch pub-sub))

;; TODO this fake event [:simulation-progress %] its only needed because the client can't join topics yet
(defn- notify-progress [pub-sub progress]
  (publish pub-sub "push.simulation-progress" [:simulation-progress progress]))

(def default-params {:tickers {"BTCUSD" {:initial-price 5000}}
                     :initial-funds {"USD" 10000 "BTC" 2}
                     :n-traders 10
                     :n-orders-per-trader 5
                     :max-ms-between-orders 100
                     :max-pos-size-pct 0.3
                     :spread-around-better-price [0.2 0.005]})

(defn start-simulator [{:keys [running pub-sub dispatch]} params]
  (when (compare-and-set! running false true)
    (log/info "Starting simulation with params" params)
    (thread
      (let [{:keys [n-traders n-orders-per-trader] :as params} (merge default-params params)
            progress (new-progress-tracker {:total (* n-traders n-orders-per-trader)
                                            :on-progress (partial notify-progress pub-sub)
                                            :notify-every-ms 250})]
        (try
          (run-simulation params {:dispatch dispatch :running running :progress progress})
          (catch Exception e
            (log/error e)
            (throw e))
          (finally
            (finish! progress)
            (reset! running false)))))))

(defn stop-simulator [{:keys [running]}]
  (reset! running false)
  (log/info "Stopping simulation"))
