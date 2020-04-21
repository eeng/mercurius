(ns mercurius.util.progress)

(defn- progress-percent [value total]
  (min (/ value total 1.0) 1.0))

(defn new-progress-tracker [{:keys [total on-progress] :or {on-progress identity}}]
  (let [current-value (atom 0)
        progress! (fn []
                    (let [[_ new-val] (swap-vals! current-value inc)]
                      (on-progress (progress-percent new-val total))))
        current-progress (fn []
                           (progress-percent @current-value total))]
    {:progress! progress!
     :current-progress current-progress}))

(comment
  (let [{:keys [progress! current-progress]} (new-progress-tracker {:total 10 :on-progress println})]
    (dotimes [_ 5]
      (progress!))
    (println "final:" (current-progress))))
