(ns mercurius.wallets.adapters.repositories.in-memory-wallet-repository-test
  (:require [clojure.test :refer [deftest testing is]]
            [mercurius.support.asserts]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [save-wallet load-wallet fetch-wallet get-user-wallets]]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [new-in-memory-wallet-repo]]))

(deftest in-memory-wallet-repository-test
  (testing "save-wallet should create or update the wallet"
    (let [wallet1 {:id 1 :user-id "456" :currency "USD"}
          wallet2 (assoc wallet1 :balance 9)
          repo (new-in-memory-wallet-repo)]

      (save-wallet repo wallet1)
      (is (= wallet1 (load-wallet repo "456" "USD")))

      (save-wallet repo wallet2)
      (is (= wallet2 (load-wallet repo "456" "USD")))))

  (testing "load-wallet creates the wallet if it doesn't exists and returns it"
    (let [wallet {:user-id "456" :currency "USD" :balance 0}
          repo (new-in-memory-wallet-repo)]
      (is (match? wallet (load-wallet repo "456" "USD")))
      (is (match? wallet (load-wallet repo "456" "USD")))
      (is (match? [wallet] (get-user-wallets repo "456")))))

  (testing "fetch-wallet raises an error if it doesn't exists"
    (let [wallet {:user-id "456" :currency "USD" :balance 0}
          repo (new-in-memory-wallet-repo)]
      (is (thrown-with-data? {:type :wallet/not-found} (fetch-wallet repo "456" "USD")))
      (save-wallet repo wallet)
      (is (match? wallet (fetch-wallet repo "456" "USD"))))))
