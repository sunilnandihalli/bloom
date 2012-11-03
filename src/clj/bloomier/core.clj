(ns bloomier.core
  (:import [com.github.sunilnandihalli.bloomier BloomFilter$Processor BloomFilter$Client BloomFilter$Iface]
           [org.apache.thrift TException]
           [org.apache.thrift.protocol TBinaryProtocol TProtocol]
           [org.apache.thrift.server TServer TSimpleServer TThreadPoolServer TThreadPoolServer$Args]
           [org.apache.thrift.transport TServerSocket TServerTransport])
  (:require [debug :as d]
            [clojure.pprint :as pp])
  (:use debug)
  (:gen-class (:main true)))

(defn -main []
  (let [req-handler (reify BloomFilter$Iface
                      (add [this membership-bf-id new-elem]
                        (display-local-bindings))
                      (count [this counting-bf-id elem]
                        (display-local-bindings) 0)
                      (contains [this membership-bf-id elem]
                        (display-local-bindings) false)
                      (dec [this counting-bf-id elem]
                        (display-local-bindings))
                      (inc [this counting-bf-id elem]
                        (display-local-bindings))
                      (bulk_contains [this list-of-membership-bf-id list-of-elems]
                        (display-local-bindings) {})
                      (aged_inc [this aging-bf-id elem]
                        (display-local-bindings))
                      (aged_dec [this aging-bf-id elem]
                        (display-local-bindings))
                      (aged_count [this aging-bf-id elem]
                        (display-local-bindings) 0)
                      (commonality_add [this commonality-bf-id elem]
                        (display-local-bindings))
                      (direct_commonality_add [this commonality-bf-id elem]
                        (display-local-bindings))
                      (is_common [this commonality-bf-id elem]
                        (display-local-bindings) false)
                      (delete_bloom_filter [this bf-id]
                        (display-local-bindings))
                      (is_bloom_filter_present [this bf-id]
                        (display-local-bindings) false))
        processor (BloomFilter$Processor. req-handler)
        server-transport (TServerSocket. 9090)
        server (TThreadPoolServer. (doto (TThreadPoolServer$Args. server-transport)
                                     (.processor processor)))]
    (.serve server)))

#_ (let [{:keys [a b c]} {:a 10 :b 20 :c 40}]
 (d/d [a b c ]))


(defn fib [n]
  (cond
   (= n 0) 0
   (= n 1) 1
   :else (+ (fib (- n 1)) (fib (- n 2)))))

#_ (fib 10)