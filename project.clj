(defproject maze "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"
                 "project" "file:repo"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]
                 [org.clojure/data.generators "0.1.0"]
                 [com.taoensso/timbre "2.4.1"]
                 [clj-time "0.5.1"]
                 [seesaw "1.4.0"]
                 [logging "0.1.0-SNAPSHOT"]]
  :main maze.core
   :jar-name "maze.jar"
  ;; As above, but for uberjar.
  :uberjar-name "maze-standalone.jar"
  ;; Options to pass to java compiler for java source,
  ;; exactly the same as command line arguments to javac.
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  ;; Leave the contents of :source-paths out of jars (for AOT projects).
  :omit-source true
  :jvm-opts ["-Xmx1g" "-Xdock:name=The Maze"]
  :profiles {:dev
             {:dependencies [[midje "1.5.1"]
                             [bultitude "0.1.7"]
                             [criterium "0.4.1"]
                             ;; <<<<==== fix
                             [lein-midje "3.1.0"]]
              :plugins [[lein-midje "3.1.0"]]}})
;; -Xdock:name="YourNameHere"
