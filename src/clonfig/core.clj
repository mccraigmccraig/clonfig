(ns ^{:doc "simple app config from environment variables"}
  clonfig.core
  (:use clonfig.impl))

(defn apply-if-string
  [parse-fn]
  (fn [v] (if (string? v) (parse-fn v) v)))

(def default-value-processors {:bool (apply-if-string #(Boolean/parseBoolean %))
                               :int (apply-if-string #(Integer/parseInt %))
                               :long (apply-if-string #(Long/parseLong %))
                               :bigint (apply-if-string bigint)
                               :bigdec (apply-if-string bigdec)
                               :float (apply-if-string #(Float/parseFloat %))
                               :double (apply-if-string #(Double/parseDouble %))
                               :keyword (apply-if-string keyword)
                               :read (apply-if-string read-string)})

(defn read-config
  "given a map of config attrs and their default specs and an optional map of value processors,
   read environment variables and apply post-processors to produce a vanilla hash-map of
   config values.

   value-processors is an optional keyword-keyed map of single-argument functions which process
   the string-value of an environment-variable or default into the intended config value. it
   defaults to clonfig.core/default-value-processors

   config-defaults is a map of keyword config attribute names to attribute default specs.

   attribute default specs may take one of the forms :

   [default-value value-processor-key-or-post-processor-fn]
   post-processor-fn
   default-value

   where -
   value-processor-key: identifies a value-processor from the value-processors map
   post-processor-fn: a function with arguments [config value] which is called to
     retrieve the value of the config attribute. it may use the config map to
     retrieve the value of other attributes in order to calculate the attribute value
   default-value: a simple default value with no post processing"
  [config-defaults & {:keys [value-processors]}]
  (-> (delayed-config config-defaults :value-processors (or value-processors default-value-processors) )
      deref-delayed-config)
)
