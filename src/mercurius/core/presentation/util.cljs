(ns mercurius.core.presentation.util
  (:require [re-frame.core :refer [dispatch subscribe]]))

(def >evt dispatch)
(def <sub (comp deref subscribe))
