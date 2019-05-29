(defproject metabase/gremlin-driver "0.1.0-SNAPSHOT"
  :min-lein-version "2.5.0"
  :dependencies [[org.apache.tinkerpop/gremlin-driver "3.4.1"]
                 [duct/core "0.7.0"]
                 [duct/module.logging "0.4.0"]]

  :jvm-opts
  ["-XX:+IgnoreUnrecognizedVMOptions"]
  :plugins [[duct/lein-duct "0.12.1"]]
  :prep-tasks ["javac" "compile" ["run" ":duct/compiler"]]
  :middleware [lein-duct.plugin/middleware]

  :profiles
  {:provided
   {:dependencies
    [[org.clojure/clojure "1.10.0"]
     [metabase-core "1.0.0-SNAPSHOT"]]}

   :dev [:project/dev :profiles/dev]
   :repl {:prep-tasks ^:replace ["javac" "compile"]}
   :profiles/dev {:main ^:skip-aot metabase.driver.gremlin

                  }
   :project/dev {:dependencies
                 [[org.apache.tinkerpop/gremlin-server "3.4.1"]
                  [integrant/repl "0.3.1"]
                  [eftest "0.5.7"]]
                 :source-paths ["dev/src"]
                 :resource-paths ["dev/resources"]}
   :uberjar
   {:auto-clean    true
    :aot           :all
    :omit-source   true
    :prep-tasks    ^:replace ["javac" "compile"]
    :javac-options ["-target" "1.8"]
    :target-path   "target/%s"
    :uberjar-name  "gremlin.metabase-driver.jar"}})
