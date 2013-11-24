(defproject maze "0.2.0-SNAPSHOT"
  :description "This is a simple Maze app, written to experiment with core.async"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"project" "file:repo"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [org.clojure/data.generators "0.1.0"]
                 [com.taoensso/timbre "2.4.1"]
                 [clj-time "0.5.1"]
                 [seesaw "1.4.0"]
                 [prismatic/schema "0.1.8"]
                 [org.clojure/data.fressian "0.2.0"]
                 [logging "0.1.0-SNAPSHOT"]]
  :main maze.core
  :jar-name "maze.jar"
  ;; As above, but for uberjar.
  :uberjar-name "maze-standalone.jar"
  ;; Options to pass to java compiler for java source,
  ;; exactly the same as command line arguments to javac.
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
  ;; Leave the contents of :source-paths out of jars (for AOT projects).
  :omit-source true
  :jvm-opts ["-Xmx1g" "-Xdock:name=The Clojure Maze"]
  :profiles {:dev
             {:dependencies [[midje "1.5.1"]
                             [bultitude "0.1.7"]
                             [criterium "0.4.1"]
                             ;; <<<<==== fix
                             [lein-midje "3.1.0"]]
              :plugins [[lein-midje "3.1.0"]]}})
