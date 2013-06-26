(defproject clonfig "0.1.1"
  :description "simple java properties, lein profiles and environment variable based config for clojure apps"
  :url "https://github.com/lambder/clonfig"
  :dependencies [[org.clojure/clojure "1.3.0"][environ "0.4.0"]]  
    
  :profiles {
             :dev {
                   :dependencies [[midje "1.5.1"]]
                   :plugins [[lein-midje "3.0.1"]]}})
