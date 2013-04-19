(ns clonfig.impl-test
  (:use clonfig.core
        clonfig.impl
        midje.sweet))

(facts "about compose-ev-prefix"
  (compose-ev-prefix) => []
  (compose-ev-prefix :foo) => [:foo]
  (compose-ev-prefix :foo nil) => [:foo]
  (compose-ev-prefix :foo :bar) => [:foo :bar]
  (compose-ev-prefix [:foo :bar] :baz) => [:foo :bar :baz]
  (compose-ev-prefix [:foo :bar] nil) => [:foo :bar]
  (compose-ev-prefix [:foo :bar] [:baz nil]) => [:foo :bar :baz])

(facts "about config-ev-name"
  (config-ev-name :foo) => "FOO"
  (config-ev-name :foo-bar) => "FOO_BAR"
  (config-ev-name :foo :ev-prefix :bar) => "BAR_FOO"
  (config-ev-name :foo :ev-prefix [:bar :baz]) => "BAR_BAZ_FOO")

(facts "about post-processor-fn"
  ((post-processor-fn default-value-processors :keyword) ...config... "bloo") => :bloo
  (post-processor-fn default-value-processors :boo) => (throws RuntimeException)
  (let [pp (fn [config val] [config val])]
    (post-processor-fn default-value-processors pp) => (exactly pp))
  (post-processor-fn default-value-processors 100) => (throws RuntimeException))

(facts "about destructure-attr-defaults"
  (destructure-attr-defaults {:foo 100}) => {:foo 100}
  (destructure-attr-defaults 100) => [100, nil]
  (destructure-attr-defaults [100 :keyword]) => [100 :keyword]
  (let [pp (fn [config val] [config val])]
    (destructure-attr-defaults pp) => [nil pp]
    (destructure-attr-defaults [100, pp]) => [100 pp]))

(facts "about value-delay"
  (let [config-promise (delay {:bar (delay 500)})]
    @(value-delay config-promise :foo [100 nil] :value-processors default-value-processors) => 100
    @(value-delay config-promise :foo ["boo" :keyword] :value-processors default-value-processors) => :boo
    @(value-delay config-promise :foo ["100" :int] :value-processors default-value-processors) => 100
    @(value-delay config-promise :foo [100 (fn [config val] (inc val))] :value-processors default-value-processors) => 101
    @(value-delay config-promise :foo [100 (fn [config val] (+ @(config :bar) val))] :value-processors default-value-processors) => 600))

(facts "about value delay with environment variables"
  (let [config-promise (delay {})]
    @(value-delay config-promise :foo [100 :int] :value-processors default-value-processors) => 200

    (provided
      (read-ev "FOO") => "200")))

(facts "about value-delay with nested config-defaults"
  (let [config-promise (delay {})]
    @(value-delay config-promise :foo {:bar 100}) => {:bar 100}
    (provided
      (delayed-config {:bar 100} :value-processors nil :ev-prefix '(:foo)) => {:bar (delay 100)})))

(facts "about value-delay with nested config-defaults and environment variables"
  (let [config-promise (delay {})]
    @(value-delay config-promise :foo {:bar [100 :int]} :value-processors default-value-processors) => {:bar 200}
    (provided
      (read-ev "FOO_BAR") => "200")))


(facts "about delayed-config"
  (let [config-defaults {:foo 100
                         :bar ["500" :keyword]
                         :baz [1000 (fn [config val] (inc val))]
                         :boo [2000 (fn [config val] (+ @(:baz config) val))]}
        dc (delayed-config config-defaults :value-processors default-value-processors)]
    @(:foo dc) => 100
    @(:bar dc) => :500
    @(:baz dc) => 1001
    @(:boo dc) => 3001))
