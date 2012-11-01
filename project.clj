(defproject bloomier "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/core.cache "0.6.2"]
                 [com.novemberain/monger "1.3.1"]
                 [cheshire "4.0.3"]
                 [org.clojure/clojure "1.4.0"]]
  :resource-paths  ["/usr/local/lib/libthrift-0.8.0.jar"
                    "/usr/local/lib/slf4j-log4j12-1.5.8.jar" "/usr/local/lib/slf4j-api-1.5.8.jar"]
  :source-paths ["src/clj/"]
  :java-source-paths [ "src/java/"]
  :main bloomier.core)
