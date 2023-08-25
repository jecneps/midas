(defproject midas "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.10.0"]

                 [thheller/shadow-cljs "2.23.3"]
                 [binaryage/devtools "0.9.7"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [ring/ring-core "1.8.2"]
                 [reagent "1.2.0"]
                 [metosin/reitit "0.5.11"]
                 [re-frame "1.3.0"]
                 [lambdaisland/fetch "1.3.74"]
                 [day8.re-frame/re-frame-10x "1.6.0"]]
  :main ^:skip-aot midas.backend.main.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
