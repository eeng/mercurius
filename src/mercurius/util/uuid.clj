(ns mercurius.util.uuid)

(defn uuid []
  (str (java.util.UUID/randomUUID)))
