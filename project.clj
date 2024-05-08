(defproject etl "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :jvm-opts ["-Xmx2096M"]
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojars.aregee/etlp "0.3.4-SNAPSHOT"]
                 [environ "1.2.0"]
                 [org.clojure/data.csv "1.1.0"]
                 [cli-matic "0.5.4"]
                 [org.clojure/tools.logging "1.2.4"]]
  :plugins [[lein-cloverage "1.2.2"]
            [com.github.clj-kondo/lein-clj-kondo "0.2.5"]]
  :aliases {"clj-kondo-deps" ["with-profile" "+test" "clj-kondo" "--copy-configs" "--dependencies" "--parallel" "--lint" "$classpath"]
            "clj-kondo-lint" ["do" ["clj-kondo-deps"] ["with-profile" "+test" "clj-kondo"]]}
  :main ^:skip-aot etl.core
  :aot [etl.core]
  :repl-options {:init-ns etl.core-test}
  :clean-targets [:target-path "target/uberjar/classes" "target/uberjar/stale"]
  :profiles {:uberjar {:aot :all}})
