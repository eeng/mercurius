(ns mercurius.core.presentation.util
  (:require [re-frame.core :as rf]))

(def >evt rf/dispatch)
(def <sub (comp deref rf/subscribe))

(def standard-interceptors  [(when ^boolean goog.DEBUG rf/debug)])

(defn reg-event-db
  ([id handler-fn]
   (rf/reg-event-db id standard-interceptors handler-fn))
  ([id interceptors handler-fn]
   (rf/reg-event-db
    id
    [standard-interceptors interceptors]
    handler-fn)))
