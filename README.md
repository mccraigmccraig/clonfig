## clonfig ##

[![Build Status](https://secure.travis-ci.org/mccraigmccraig/clonfig.png)](http://travis-ci.org/mccraigmccraig/clonfig)


simple environment variable based configuration for clojure apps.

config attributes are defined in a map, along with defaults and post-processor functions
(to parse or otherise make-ready config values for consumption by the program).

* attribute values are read from corresponding environment variables
* default values can be specified which are used if there is no environment variable
* value post-processors can be specified with a keyword, and transform the value
* post-processor functions can be passed directly and are given a map of delays for
  config-values and the value (default or environment variable) and can therefore
  produce config attributes which depend on the value of other config attributes
* maps of config-defaults can be nested : a nested map will have a prefix on corresponding
  environment variables as specified by the key in the containing map, so in the example below
  the smtp port can be set with environment variable SMTP_PORT

## Usage ##

add the dependency to your project.clj

    [clonfig "0.2.0"]

define any environment variables you want before running a clojure process

    ENVIRONMENT=production SMTP_PORT=465 lein repl

the read-config function produces a simple map of config attributes

    (use 'clonfig.core)

    (def config-defaults {:environment "development"
                          :port [8080 :int]
                          :database-url ["postgresql://localhost/"
                                         (fn [config val] (str val @(:environment config)))]
                          :smtp { :host "localhost"
                                  :port [25 :int]}})

    (def config (read-config config-defaults))

    (:environment config)  ;; "production"
    (:port config)         ;; 8080
    (:database-url config) ;; "postgresql://localhost/production"
    (get-in config [:smtp :host]) ;; "localhost"
    (get-in config [:smtp :port]) ;; 465

## License ##

Copyright (C) 2013 mccraigmccraig

Distributed under the Eclipse Public License, the same as Clojure.
