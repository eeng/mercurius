(ns mercurius.core.adapters.web.helpers)

(defn ok [body]
  {:status 200 :body body})
