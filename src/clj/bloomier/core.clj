(ns bloomier.core
  (:require bloomier.handler)
  (:import [com.github.sunilnandihalli.bloomier BloomFilter$Processor BloomFilter$Client]
           [org.apache.thrift TException]
           [org.apache.thrift.protocol TBinaryProtocol TProtocol]
           [org.apache.thrift.server TServer TSimpleServer TThreadPoolServer]
           [org.apache.thrift.transport TServerSocket TServerTransport]
           [bloomier handler])
  (:require [debug :as d])
  (:use debug)
  (:gen-class (:main true)))

(defn -main []
  (let [req-handler (reify com.github.sunilnandihalli.bloomier.BloomFilter$Iface
                      (add [this membership-bf-id new-elem]
                        (display-local-bindings))
                      (count [this counting-bf-id elem])
                      (contains [this membership-bf-id elem])
                      (dec [this counting-bf-id elem])
                      (inc [this counting-bf-id elem])
                      (bulk_contains [this list-of-membership-bf-id list-of-elems])
                      (aged_inc [this aging-bf-id elem])
                      (aged_dec [this aging-bf-id elem])
                      (aged_count [this aging-bf-id elem])
                      (commonality_add [this commonality-bf-id elem])
                      (direct_commonality_add [this commonality-bf-id elem])
                      (is_common [this commonality-bf-id elem])
                      (delete_bloom_filter [this bf-id])
                      (is_bloom_filter_present [this bf-id]))
        processor (BloomFilter$Processor. req-handler)
        server-transport (TServerSocket. 9090)
        #_ server
       
       #_ (TSimpleServer. (doto (org.apache.thrift.server.TSimpleServer$Args. server-transport)
                            (.processor processor)))
        server (TThreadPoolServer. (doto (org.apache.thrift.server.TThreadPoolServer$Args. server-transport)
                                     (.processor processor)))]
    (.serve server)))

(let [])

#_ (let [{:keys [a b c]} {:a 10 :b 20 :c 40}]
 (d/d [a b c ]))


(defn fib [n]
  (cond
   (= n 0) 0
   (= n 1) 1
   :else (+ (fib (- n 1)) (fib (- n 2)))))

#_ (fib 10)