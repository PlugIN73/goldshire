(ns goldshire.core-test
  (:require [clojure.test :refer :all]
            [goldshire.core :refer :all]))

(deftest a-test
  (testing "ruby eval"
    (is (= 2 (ruby-eval "1 + 1")))))
