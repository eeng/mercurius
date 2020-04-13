(ns mercurius.util.ring)

(defn ok [body]
  {:status 200 :body body})
