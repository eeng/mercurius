(ns mercurius.core.infraestructure.web.helpers)

(defn ok [body]
  {:status 200 :body body})
