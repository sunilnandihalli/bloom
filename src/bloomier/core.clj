(ns bloomier.core
  (:require [plaid-penguin.server :as pp-server]
            [carmine.core :as r]
            [plaid-penguin.client :as pp-client]
            )

 (:import [bloomier UserHistory$Iface UserHistory$Processor UserHistory$Client]
           [bloomier Query Result])
  (:gen-class))

(def pool (r/make-conn-pool :test-while-idle? true))
(def spec-server1 (r/make-conn-spec :host     "127.0.0.1"
                                    :port     6379))
(defmacro redis
  "Basically like (partial with-conn pool spec-server1)."
  [& body] `(r/with-conn pool spec-server1 ~@body))

(defn visit-url [uid url]
  (redis (r/hincrby (str uid) (str url) "1")))
(defn url-visit-count [uid url]
  (Integer/parseInt (redis (r/hget (str uid) (str url)))))

(defn query-to-map [^bloomier.Query q]
  {:user-id (.getUid q) :content-hash (.getContent q)})

(defn create-result [user-id content-hash url-visit-cnt]
  (doto (Result.)
    (.setUid user-id)
    (.setContent content-hash)
    (.setCount url-visit-cnt)))
;; Subclass the handler.
(def ^:dynamic *processor* 
  (UserHistory$Processor. 
   (proxy [UserHistory$Iface] []
     (count [user-id content-hash]
       (create-result user-id content-hash (url-visit-count user-id content-hash)))
     (visit [user-id content-hash]
       (visit-url user-id content-hash))))) ;; It just returns OK.

;; Start the server.
(defn start-bloomier-server [port]
  "Given a port, starts a multi-threaded server, and returns it."
  (let [server (pp-server/create-multi-threaded port *processor*)]
    ;; Create and start a thread, to stop the server, call 'stop' on it.
    (.start (Thread. #(.serve server)))
    server))

;; Initialize both server and client, and make an RPC call.
(def ^:dynamic *num-test-users* 100)
(def ^:dynamic *num-test-urls* 100)
(def ^:dynamic *max-url-hash-val* 10000)
(def ^:dynamic *max-urls-per-user* 20)
(def ^:dynamic *max-url-visits-per-user* 5)

#_(let [{:keys [x y z a] :as w {:keys [u v] :as coord} :coord} {:x 100 :y 200 :z 3 :a 20 :coord {:u 5  :v 7}}]
    {:w w :x x :y  y :z z :a a :u u :v v :coord coord})

(defn test-redis-server-interface []
  (let [all-urls (vec (take *num-test-urls* (distinct (repeatedly #(rand-int *max-url-hash-val*)))))
        random-url-count-pairs (into {} (map (fn [user-id]
                                          (vector user-id
                                                  (into {} (repeatedly (inc (rand-int *max-urls-per-user*))
                                                                       #(vector (rand-nth all-urls)
                                                                                (inc (rand-int *max-url-visits-per-user*)))))))
                                        (range *num-test-users*)))
        
   visit-random-url (fn [m]
                      (let [[user-id url-count-pairs] (rand-nth (vec m))
                            [url url-count] (rand-nth (vec url-count-pairs))]
                        (visit-url user-id url)
                        (not-empty
                         (if (> url-count 1) (update-in m [user-id url] dec)
                             (let [m1 (update-in m [user-id] dissoc url)]
                               (if (empty? (m1 user-id)) (dissoc m1 user-id) m1))))))]
    (redis (r/flushdb))
    (loop [m random-url-count-pairs]
      (when m (recur (visit-random-url m))))
    (into {} (map (fn [[uid url-count-pairs]]
                    [uid (into {} (map (fn [[url count]]
                                         [url {:original count :bloom (url-visit-count uid url)}]) url-count-pairs))])
                  random-url-count-pairs))))

(defn -main [& args]
  (let [port           (Integer. (first args))
        bloomier-server (start-bloomier-server port) ;; Server.
        connection     (pp-client/create-socket "localhost" port)
        client         (pp-client/create UserHistory$Client connection)])) ;; Not all servers are required to be cleanly stoppable.


(let [{:keys [a b c ] :as w {:keys [d e f ] :as x} :g }
      {:a 1 :b 2 :c 3 :g  {:d 4 :e 5 :f 6}}]
  [a b c w x  d e  f])
(let [x 10
      {:keys [a b c]} {:a 1 :b 3 :c 4}]
  [a b c])

(map #(+ %1 %2) (range 10) (range 10 20))




(defn bloom-k [err]
  )