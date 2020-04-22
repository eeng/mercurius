(ns mercurius.util.progress
  (:require [mercurius.util.throttle :refer [throttle]]))

(defn- progress-percent [value total]
  (min (/ value total 1.0) 1.0))

(defn new-progress-tracker [{:keys [total on-progress notify-every-ms]
                             :or {on-progress identity notify-every-ms 0}}]
  (let [current-value (atom 0)
        progress! (fn [& [step]]
                    (let [on-progress (throttle on-progress {:rate-ms notify-every-ms})
                          [_ new-val] (swap-vals! current-value + (or step 1))]
                      (on-progress (progress-percent new-val total))))
        finish! (fn []
                  (reset! current-value total)
                  ;; We don't want to throllle this one.
                  (on-progress 1.0))
        current-progress (fn []
                           (progress-percent @current-value total))]
    {:progress! progress!
     :finish! finish!
     :current-progress current-progress}))

(comment
  (let [{:keys [progress! current-progress]} (new-progress-tracker {:total 10 :on-progress println})]
    (dotimes [_ 5]
      (progress!))
    (println "final:" (current-progress))))
