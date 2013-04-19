(ns clonfig.core-test
  (:use clonfig.core
        midje.sweet))

(facts "about read-config"
  (let [config-defaults {:foo 100
                         :gah ["true" :bool]
                         :blah [200 :int]
                         :bar ["500" :keyword]
                         :baz [1000 (fn [config val] (inc val))]
                         :boo [2000 (fn [config val] (+ @(:baz config) val))]}
        c (read-config config-defaults)]
    (:foo c) => 100
    (:gah c) => true
    (:blah c) => 200
    (:bar c) => :500
    (:baz c) => 1001
    (:boo c) => 3001))
