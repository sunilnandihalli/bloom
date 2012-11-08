(defproject bloomier "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/core.cache "0.6.2"]
                 [org.clojure/data.priority-map "0.0.2"]
                 [com.novemberain/monger "1.3.1"]
                 [thrift "0.8.0"]
                 [slf4j-api "1.5.8"]
                 [slf4j-log4j12 "1.5.8"]
                 [log4j "1.2.14"]
                 [cheshire "4.0.3"]
                 [org.clojure/clojure "1.4.0"]
                 [org.clojure/math.numeric-tower "0.0.1"]
                 [com.datomic/datomic-free "0.8.3595"]]
  :plugins [[lein-localrepo "0.4.1"]]
  :source-paths ["src/clj/"]
  :java-source-paths [ "src/java/"]
  :aot [bloomier.handler bloomier.core]
  :main bloomier.core
  :repositories {"sonatype-oss-public"
                 "https://oss.sonatype.org/content/groups/public/"})
