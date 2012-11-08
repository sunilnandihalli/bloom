(ns lru-datomic
  (:use [datomic.api :only [db q] :as d]))

(def uri "datomic:free://localhost:4334/bfdata")
(d/create-database uri)
(def conn (d/connect uri))
#_(defn )