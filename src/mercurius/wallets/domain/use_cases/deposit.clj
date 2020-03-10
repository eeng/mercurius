(ns mercurius.wallets.domain.use-cases.deposit
  (:require [mercurius.core.use-case :refer [UseCase]]))

(defrecord DepositUseCase [repo]
  UseCase
  (run [{:keys [repo]} command]
    (println "depositing" command "- repo:" repo)))

; (defn make-use-case [repo]
;   (fn [command]
;     (println "depositing" command "- repo:" repo)))

; (defn execute [repo command]
;   (println "depositing" command "- repo:" repo))

