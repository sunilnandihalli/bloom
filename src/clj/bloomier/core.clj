(ns bloomier.core
  (:require handler)
  (:import [com.github.sunilnandihalli.bloomier BloomFilter$Processor BloomFilter$Client]
           [org.apache.thrift TException]
           [org.apache.thrift.protocol TBinaryProtocol TProtocol]
           [org.apache.thrift.server TServer TSimpleServer TThreadPoolServer]
           [org.apache.thrift.transport TServerSocket TServerTransport]
           handler)
  (:require [debug :as d])
  (:use debug)
  (:gen-class (:main true)))




(defn -main []
  (let [handler (bloomier.handler.)
        processor (BloomFilter$Processor. handler)
        server-transport (TServerSocket. 9090)
        ;server (TSimpleServer. processor server-transport)
        server (TThreadPoolServer. processor server-transport)]
    (.serve server)))