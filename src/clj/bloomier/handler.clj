(ns bloomier.handler
  (:import [com.github.sunilnandihalli.bloomier BloomFilter$Iface])
  (:use debug)
  (:gen-class
   (:implements BloomFilter$Iface)))

(defn -add [this membership-bf-id new-elem]
  (display-local-bindings))
(defn -count [this counting-bf-id elem])
(defn -contains [this membership-bf-id elem])
(defn -dec [this counting-bf-id elem])
(defn -inc [this counting-bf-id elem])
(defn -bulk_contains [this list-of-membership-bf-id list-of-elems])
(defn -aged_inc [this aging-bf-id elem])
(defn -aged_dec [this aging-bf-id elem])
(defn -aged_count [this aging-bf-id elem])
(defn -commonality_add [this commonality-bf-id elem])
(defn -direct_commonality_add [this commonality-bf-id elem])
(defn -is_common [this commonality-bf-id elem])
(defn -delete_bloom_filter [this bf-id])
(defn -is_bloom_filter_present [this bf-id])