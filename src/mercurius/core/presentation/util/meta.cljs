(ns mercurius.core.presentation.util.meta)

(defn csrf-token []
  (-> js/document (.querySelector "meta[name='csrf-token']") .-content))
