(ns bloomier.core
  (:require [bloomier handler])
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
  (let [handler (bloomier.handler.)
        processor (BloomFilter$Processor. handler)
        server-transport (TServerSocket. 9090)
        #_server
       
       #_ (TSimpleServer. (doto (org.apache.thrift.server.TServer.Args. server-transport)
                          (.processor processor)))
        server (TThreadPoolServer. (doto (org.apache.thrift.server.TThreadPoolServer.Args. server-transport)
                                     (.processor processor)))]
    (.serve server)))