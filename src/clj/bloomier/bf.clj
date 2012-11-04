(ns bloomier.bf
  (:require [debug :as d]))

; assuming cell-width will always be less than 64
(defrecord cell-array [^clojure.core.Vec long-vec ^long cell-width ^long num-cells])
(defn get-cell [{:keys [long-vec cell-width num-cells]} cell-id]
  (let [blk-width 64
        bit-offset (* cell-width cell-id)
        offset-start (mod bit-offset blk-width)
        offset-end (+ offset-start cell-width)
        long-id-end (let [x (quot bit-offset blk-width)]
                      (if (> offset-end blk-width) (inc x) x))]
    (loop [bits-read 0 i (dec offset-end) long-id long-id-end cur-long (long-vec long-id-end) cur-val 0]
      (if-not (< bits-read cell-width) cur-val
              (let [new-bits-read (inc bits-read)
                    new-val (if (bit-test cur-long i) (bit-set cur-val bits-read) cur-val)]
                (if (> i 0)
                  (recur new-bits-read (dec i) long-id cur-long new-val)
                  (recur new-bits-read (dec blk-width) (dec long-id) (if (< new-bits-read cell-width)
                                                                       (long-vec (dec long-id))) new-val)))))))
(defn inc-cell [{:keys [long-vec cell-width num-cells]} cell-id]
  (let [blk-width 64
        bit-offset (* cell-width cell-id)
        offset-start (mod bit-offset blk-width)
        offset-end (+ offset-start cell-width)
        long-id-end (let [x (quot bit-offset blk-width)]
                      (if (> offset-end blk-width) (inc x) x))]
    (loop [bits-read 0 i (dec offset-end) long-id long-id-end
           cur-long (long-vec long-id-end) carry true cur-long-vec long-vec]
      (if-not carry (cell-array. (assoc cur-long-vec long-id cur-long) cell-width num-cells)
              (let [is-set (bit-test cur-long i)
                    new-long (if is-set (bit-clear cur-long i) (bit-set cur-long i))
                    new-bits-read (inc bits-read)]
                (d/display-local-bindings)
                (if (> i 0)
                  (recur new-bits-read (dec i) long-id new-long is-set cur-long-vec)
                  (recur new-bits-read (dec blk-width) (dec long-id)
                         (if (< new-bits-read cell-width) (cur-long-vec (dec long-id)))
                         is-set (assoc cur-long-vec long-id cur-long))))))))

(defn clear-cell [{:keys [long-vec cell-width num-cells]} cell-id]
  (let [blk-width 64
        bit-offset (* cell-width cell-id)
        offset-start (mod bit-offset blk-width)
        offset-end (+ offset-start cell-width)
        long-id-end (let [x (quot bit-offset blk-width)]
                      (if (> offset-end blk-width) (inc x) x))]
    (loop [bits-read 0 i (dec offset-end) long-id long-id-end cur-long (long-vec long-id-end)
           cur-long-vec long-vec]
      (if-not (< bits-read cell-width) (cell-array. (assoc cur-long-vec long-id cur-long) cell-width num-cells)
              (let [new-long (bit-clear cur-long i)
                    new-bits-read (inc bits-read)]
                (if (> i 0)
                  (recur new-bits-read (dec i) long-id new-long cur-long-vec)
                  (recur new-bits-read (dec blk-width) (dec long-id)
                         (if (< new-bits-read cell-width) (long-vec (dec long-id)))
                         (assoc cur-long-vec long-id new-long))))))))

(defn long-vec [{:keys [num-cells] :as cellarray}] (mapv #(get-cell cellarray %) (range num-cells)))

(defn make-cell-array [^long cell-width ^long num-cells]
  (let [num-bits-long 64
        num-bits (* cell-width num-cells)
        long-vec-length (let [x (quot num-bits num-bits-long)]
                          (if (= 0 (mod num-bits num-bits-long))
                            x (inc x)))]
    (->cell-array (apply vector-of :long (repeat long-vec-length 0)) cell-width num-cells)))
#_(map long-vec (take 3 (iterate (fn [x] (reduce (fn [old-s i] (inc-cell old-s i)) x (range 4))) s)))
#_(map long-vec (take 3 (iterate #(inc-cell % 0) s)))
#_ (let [x (make-cell-array 4 30)]
     (long-vec x))
#_ ()

#_ ()
#_ (defprotocol )