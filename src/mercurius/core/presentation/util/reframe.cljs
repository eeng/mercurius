(ns mercurius.core.presentation.util.reframe
  (:require [re-frame.core :as rf]
            [clojure.spec.alpha :as s]))

(def >evt rf/dispatch)
(def <sub (comp deref rf/subscribe))

(def debug? ^boolean goog.DEBUG)

(s/check-asserts true)

(defn validate-db [spec]
  (rf/->interceptor
   :id :validate-db
   :after (fn [{{:keys [:event :re-frame.std-interceptors/untrimmed-event]} :coeffects
                {:keys [:db]} :effects :as context}]
            (when (and (s/check-asserts?) db (not (s/valid? spec db)))
              (js/console.log "DB:" db)
              (throw (js/Error. (str "DB is invalid after event"
                                     (or untrimmed-event event) "\n"
                                     (subs (s/explain-str spec db) 0 1000)))))
            context)))

(def standard-interceptors [(when debug? rf/debug)
                            (validate-db :app/db)])

(defn reg-event-db
  ([id handler-fn]
   (rf/reg-event-db id standard-interceptors handler-fn))
  ([id interceptors handler-fn]
   (rf/reg-event-db
    id
    [standard-interceptors interceptors]
    handler-fn)))
