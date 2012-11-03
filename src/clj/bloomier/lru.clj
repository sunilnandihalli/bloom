(ns bloomier.lru
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.data.priority-map :as pm])
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId])
  (:use clojure.core.cache))

(let [^MongoOptions opts (mg/mongo-options :threads-allowed-to-block-for-connection-multiplier 300)
      ^ServerAddress sa  (mg/server-address "127.0.0.1" 27017)]
  (mg/connect! sa opts))

#_(mg/with-db (mg/get-db "hello")
    (mc/insert "some_collection" {:a 10 :b 20 :c {:d 30 :e 40} :_id (ObjectId.)}))