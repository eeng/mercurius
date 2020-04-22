(ns mercurius.util.throttle)

(defn throttle
  "Throttle calls to the function to occour only once every `rate-ms`."
  [f {:keys [rate-ms]}]
  (let [last-called-at (atom nil)]
    (fn [& args]
      (let [now (System/currentTimeMillis)]
        (when (or (nil? @last-called-at)
                  (>= (- now @last-called-at) rate-ms))
          (reset! last-called-at now)
          (apply f args))))))

(comment
  (let [tprintln (throttle println {:rate-ms 50})]
    (dotimes [i 10]
      (tprintln "running" i)
      (Thread/sleep 20))))
