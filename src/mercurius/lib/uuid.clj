(ns mercurius.lib.uuid)

(defn uuid []
  (str (java.util.UUID/randomUUID)))
