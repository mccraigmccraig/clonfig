(ns ^{:doc "clonfig implementation"}
  clonfig.impl
  (:require [clojure.string :as str]))

(defn compose-ev-prefix
  "compose an environment variable prefix consisting of one or more strings or keywords
   or sequences thereof"
  [& args]
  (->> args
       flatten
       (filter identity)))

(defn config-ev-name
  "given a keyword name for a config attribute,
   convert it to the corresponding environment variable name.
   :foo => \"FOO\"
   :foo-bar => \"FOO_BAR\"
   if ev-prefix is given it will be converted similarly, and prefixed
   to the converte attr_name, joined with _"
  [attr-name & {:keys [ev-prefix]}]
  (->> [ev-prefix attr-name]
       compose-ev-prefix
       (map name)
       (str/join "_")
       .toUpperCase
       (#(str/replace % #"-" "_"))))

(defn config-ev
  "given a keyword name for a config attribute, lookup the associated environment variable"
  [attr-name & {:keys [ev-prefix]}]
  (System/getenv (config-ev-name attr-name :ev-prefix ev-prefix)))

(defn post-processor-fn
  "given a map of keyword keyed value-processors and a post-processor which is either a keyword
   to look up a value-processor function or a post-processor function,
   returns a post-processor function.
   value-processor functions take a single argument: [value].
   post-processor functions take two arguments: [config value] and can therefore look up
   other config values from the config map"
  [value-processors post-processor]
  (cond
   (and (keyword? post-processor) (post-processor value-processors))
   (fn [config val] ((post-processor value-processors) val))

   (keyword? post-processor)
   (throw (RuntimeException. (str "unknown post-processor: " post-processor)))

   (fn? post-processor)
   post-processor

   true
   (throw (RuntimeException. (str "post-processor must be a keyword or (fn [config val]...)")))))

(defn destructure-attr-defaults
  "destructures an attribute default to a [def-val post-processor] pair, or a map of nested config-defaults.
   the attribute defaults
   may already be a pair, or it may be a lone default-value or a lone post-processor function
   or a nested config-defaults map"
  [attr-defaults]
  (cond
   (map? attr-defaults) attr-defaults
   (vector? attr-defaults) attr-defaults
   (fn? attr-defaults) [nil attr-defaults]
   true [attr-defaults nil]))

(declare delayed-config)
(declare deref-delayed-config)

(defn value-delay
  "given a promise of config, and attr-name and defaults,
   returns a delay containing a function returning the value. if there is no
   post-processor then the delay returns the value directly, gotten from
   environment-variable or default-value.
   if there is a post-processor then it is called with [config value] parameters to
   give the value returned by the delay"
  [value-processors config-promise attr-name attr-defaults & {:keys [ev-prefix]}]
  (if (map? attr-defaults)
    (delay (-> (delayed-config attr-defaults
                               :value-processors value-processors
                               :ev-prefix (compose-ev-prefix ev-prefix attr-name))
               deref-delayed-config))
    (let [[def-val post-processor] attr-defaults]
      (-> attr-name
          config-ev
          (or def-val)
          ((fn [val]
             (if post-processor
               (delay ((post-processor-fn value-processors post-processor) @config-promise val))
               (delay val))))))))

(defn delayed-config
  "given a map of keyword named config attributes and their defaults, return
   a map of delays to the attribute values"
  [config-defaults & {:keys [value-processors ev-prefix]}]
  (let [config-promise (promise)]
    (->>
     config-defaults
     (map (fn [[attr-name defaults]]
            [attr-name
             (value-delay value-processors config-promise attr-name (destructure-attr-defaults defaults) :ev-prefix ev-prefix)]))
     (into {})
     (deliver config-promise)
     deref)))

(defn deref-delayed-config
  "given a map of delays to config values, deref all the delays to return a map of config values"
  [dc]
  (->> dc
       (map (fn [[attr-name attr-value-delay]] [attr-name @attr-value-delay]))
       (into {})))
