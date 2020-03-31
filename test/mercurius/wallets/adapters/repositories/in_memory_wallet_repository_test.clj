(ns mercurius.wallets.adapters.repositories.in-memory-wallet-repository-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [mercurius.support.factory :refer [build-wallet build-user-id]]
            [mercurius.support.helpers :refer [ref-trx]]
            [mercurius.wallets.domain.repositories.wallet-repository :refer [save-wallet load-wallet fetch-wallet get-user-wallets calculate-monetary-base]]
            [mercurius.wallets.adapters.repositories.in-memory-wallet-repository :refer [new-in-memory-wallet-repo]]))

(use-fixtures :each ref-trx)

(def u1 (build-user-id))
(def u2 (build-user-id))

(deftest in-memory-wallet-repository-test
  (testing "save-wallet should create or update the wallet"
    (let [wallet1 (build-wallet {:user-id "456" :currency "USD" :balance 7M})
          wallet2 (assoc wallet1 :balance 9M :version 1)
          repo (new-in-memory-wallet-repo)]

      (save-wallet repo wallet1)
      (is (match? {:balance 7M} (load-wallet repo "456" "USD")))

      (save-wallet repo wallet2)
      (is (match? {:balance 9M} (load-wallet repo "456" "USD")))))

  (testing "load-wallet creates the wallet if it doesn't exists and returns it"
    (let [repo (new-in-memory-wallet-repo)
          wallet (load-wallet repo "456" "USD")]
      (is (match? wallet (load-wallet repo "456" "USD")))
      (is (match? [wallet] (get-user-wallets repo "456")))))

  (testing "fetch-wallet raises an error if it doesn't exists"
    (let [repo (new-in-memory-wallet-repo)]
      (is (thrown-match? clojure.lang.ExceptionInfo {:type :wallet/not-found}
                         (fetch-wallet repo "456" "USD")))))

  (testing "calculate-monetary-base should create or update the wallet"
    (let [repo (new-in-memory-wallet-repo)]
      (save-wallet repo {:id 1 :user-id u1 :currency "USD" :balance 5.5M})
      (save-wallet repo {:id 2 :user-id u2 :currency "USD" :balance 4M})
      (save-wallet repo {:id 3 :user-id u1 :currency "BTC" :balance 3M})
      (is (match? {"USD" 9.5M "BTC" 3M}
                  (calculate-monetary-base repo)))))

  (testing "optimistic concurrency"
    (let [repo (new-in-memory-wallet-repo)
          _ (save-wallet repo (build-wallet {:user-id 9 :currency "USD" :balance 0}))
          w1 (fetch-wallet repo 9 "USD")
          w2 (fetch-wallet repo 9 "USD")]
      (save-wallet repo (update w1 :balance + 10))
      (is (thrown-match? clojure.lang.ExceptionInfo {:type :stale-object-error}
                         (save-wallet repo (update w2 :balance + 11))))
      (is (match? {:balance 10M} (fetch-wallet repo 9 "USD"))))))
