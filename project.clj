(def shared
  '[])

(defproject clonfig "0.2.0"
  :description "simple environment variable based config for clojure apps"

  :min-lein-version "2.0.0"

  :url "https://github.com/mccraigmccraig/clonfig"

  :plugins [[lein-midje "3.0.1"]]

  :dependencies [[org.clojure/clojure "1.5.1"]]

  :profiles {:all {:dependencies ~shared}
             :dev {:dependencies [[midje "1.5.1"]]}
             :production {}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}}
  )
