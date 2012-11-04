(ns bloomier.bf
  (:require [debug :as d]))

(defprotocol cells
  (get-cell [this cell-id])
  (inc-cell [this cell-id])
  (clear-cell [this cell-id])
  (long-vec [this]))
; assuming cell-width will always be less than 64
(defrecord cell-array [^clojure.core.Vec long-vec ^long cell-width ^long num-cells])
(extend-type cell-array
  cells
  (get-cell [{:keys [long-vec cell-width num-cells]} cell-id]
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
                    (recur new-bits-read (dec blk-width) (dec long-id) (long-vec (dec long-id)) new-val)))))))
  (inc-cell [{:keys [long-vec cell-width num-cells]} cell-id]
    (let [blk-width 64
          bit-offset (* cell-width cell-id)
          offset-start (mod bit-offset blk-width)
          offset-end (+ offset-start cell-width)
          long-id-end (let [x (quot bit-offset blk-width)]
                        (if (> offset-end blk-width) (inc x) x))]
      (loop [bits-read 0 i (dec offset-end) long-id (dec long-id-end)
             cur-long (long-vec long-id-end) carry true cur-long-vec (transient long-vec)]
        (if-not carry (cell-array. (persistent! (assoc! cur-long-vec long-id cur-long)) cell-width num-cells)
                (let [is-set (bit-test cur-long i)
                      new-long (if is-set (bit-clear cur-long i) cur-long)
                      new-bits-read (inc bits-read)]
                  (if (> i 0)
                    (recur new-bits-read (dec i) long-id new-long is-set cur-long-vec)
                    (recur new-bits-read (dec blk-width) (dec long-id)
                           (cur-long-vec (dec long-id)) is-set (assoc! cur-long-vec long-id cur-long))))))))
  (clear-cell [{:keys [long-vec cell-width num-cells]} cell-id]
    (let [blk-width 64
          bit-offset (* cell-width cell-id)
          offset-start (mod bit-offset blk-width)
          offset-end (+ offset-start cell-width)
          long-id-end (let [x (quot bit-offset blk-width)]
                        (if (> offset-end blk-width) (inc x) x))]
      (loop [bits-read 0 i (dec offset-end) long-id (dec long-id-end) cur-long (long-vec (dec long-id-end))
             cur-long-vec (transient long-vec)]
        (if-not (< bits-read cell-width) (cell-array. (persistent! (assoc! cur-long-vec long-id cur-long)) cell-width num-cells)
                (let [new-long (bit-clear cur-long i)
                      new-bits-read (inc bits-read)]
                  (if (> i 0)
                    (recur new-bits-read (dec i) long-id new-long cur-long-vec)
                    (recur new-bits-read (dec blk-width) (dec long-id) (long-vec (dec long-id))
                           (assoc! cur-long-vec long-id new-long))))))))
  (long-vec [{:keys [num-cells] :as this}] (vec (for [i (range num-cells)]
                                                  (get-cell this i)))))

(defn make-cell-array [^long cell-width ^long num-cells]
  (let [num-bits-long 64
        num-bits (* cell-width num-cells)
        long-vec-length (let [x (/ num-bits num-bits-long)]
                          (if (= 0 (mod num-bits num-bits-long))
                            x (inc x)))]
    (->cell-array (apply vector-of :long (repeat long-vec-length 0)) cell-width num-cells)))

#_ (let [x (make-cell-array 4 30)]
     (long-vec x))
#_ ()

#_ ()
#_ (defprotocol )