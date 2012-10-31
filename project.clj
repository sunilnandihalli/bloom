(defproject bloomier "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [;; [org.clojars.tavisrudd/redis-clojure "1.3.1"]
                 [com.novemberain/validateur "1.2.0"]
                 [org.clojure/math.numeric-tower "0.0.1"]
                 [org.clojure/core.cache "0.6.2"]
                 [com.novemberain/monger "1.3.1"]
                 [cheshire "4.0.3"]
                 [org.clojars.ithayer/plaid-penguin "1.0.0"]
                 [org.clojars.ithayer/thrift "0.6.1"]
                 [org.clojure/clojure "1.3.0"]]
  :java-source-path "gen_java/"
  :main bloomier.core)
