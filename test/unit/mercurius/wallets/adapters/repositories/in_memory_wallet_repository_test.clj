(ns mercurius.wallets.adapters.repositories.in-memory-wallet-repository-test
  (:require [clojure.test :refer [deftest is]]))

(deftest in-memory-wallet-repository-test
  (is (= "asdf" "asdf"))
  (is (= {:x 1} {:x 2})))
