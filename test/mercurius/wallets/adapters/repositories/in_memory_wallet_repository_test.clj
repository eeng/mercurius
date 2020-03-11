(ns mercurius.wallets.adapters.repositories.in-memory-wallet-repository-test
  (:require [clojure.test :refer [deftest testing is]]
            [support.asserts :refer [submap? submaps?]]
            [mercurius.wallets.domain.repositories.wallet-repository :as wr]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [make-in-memory-wallet-repo]]))

(deftest in-memory-wallet-repository-test
  (testing "save should create or update the wallet"
    (let [wallet1 {:id 1 :user-id "456" :currency "USD"}
          wallet2 (assoc wallet1 :balance 9)
          repo (make-in-memory-wallet-repo)]

      (wr/save repo wallet1)
      (is (= wallet1 (wr/find-or-create-by-user-id-and-currency repo "456" "USD")))

      (wr/save repo wallet2)
      (is (= wallet2 (wr/find-or-create-by-user-id-and-currency repo "456" "USD")))))

  (testing "find-or-create-by-user-id-and-currency creates the wallet if it doesn't exists and returns it"
    (let [wallet {:user-id "456" :currency "USD"}
          repo (make-in-memory-wallet-repo)]
      (is (submap? wallet (wr/find-or-create-by-user-id-and-currency repo "456" "USD")))
      (is (submap? wallet (wr/find-or-create-by-user-id-and-currency repo "456" "USD")))
      (is (submaps? [wallet] (wr/find-by-user-id repo "456"))))))
