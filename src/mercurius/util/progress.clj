(ns mercurius.util.progress
  (:require [mercurius.util.throttle :refer [throttle]]))

(defn- progress-percent [value total]
  (min (/ value total 1.0) 1.0))

(defrecord ProgressTracker [current-value total on-progress on-progress-throttled])

(defn new-progress-tracker [{:keys [total on-progress notify-every-ms]
                             :or {on-progress identity notify-every-ms 0}}]
  (map->ProgressTracker {:current-value (atom 0)
                         :total total
                         :on-progress on-progress
                         :on-progress-throttled (throttle on-progress {:rate-ms notify-every-ms})}))

(defn current [{:keys [current-value total]}]
  (progress-percent @current-value total))

(defn advance! [{:keys [current-value total on-progress-throttled]}]
  (let [[_ new-val] (swap-vals! current-value inc)]
    (on-progress-throttled (progress-percent new-val total))))

(defn finish! [{:keys [current-value total on-progress]}]
  (reset! current-value total)
  (on-progress 1.0))

(comment
  (let [progress (new-progress-tracker {:total 10 :on-progress println :notify-every-ms 50})]
    (dotimes [_ 10]
      (advance! progress)
      (Thread/sleep 20))
    (println "final:" (current progress))))
