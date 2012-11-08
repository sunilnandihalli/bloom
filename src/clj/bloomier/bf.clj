(ns bloomier.bf
  (:require [debug :as d]
            [clojure.math.numeric-tower :as m]))

(defrecord cell-array [^clojure.core.Vec long-vec ^long cell-width ^long num-cells])

(defn get-cell [{:keys [long-vec cell-width num-cells]} cell-id]
  (let [blk-width 64
        bit-offset (* cell-width cell-id)
        offset-start (mod bit-offset blk-width)
        long-id (quot bit-offset blk-width)]
    (loop [i-bit 0
           offset offset-start
           [cur-long & rest-of-longs :as cur-longs] (subvec long-vec long-id)
           cur-val 0]
      (if-not (< i-bit cell-width) cur-val
              (let [new-val (if (bit-test cur-long offset) (bit-set cur-val i-bit) cur-val)
                    [new-offset new-longs] (let [x (inc offset)]
                                             (if (< x blk-width)
                                               [x cur-longs]
                                               [0 rest-of-longs]))]
                (recur (inc i-bit) new-offset new-longs new-val))))))

(defn inc-cell [{:keys [long-vec cell-width num-cells] :as orig} cell-id]
  (let [blk-width 64
        bit-offset (* cell-width cell-id)
        offset-start (mod bit-offset blk-width)
        long-id (quot bit-offset blk-width)]
    (loop [i-bit 0
           offset offset-start
           [cur-long & rest-of-longs :as cur-longs] (subvec long-vec long-id)
           new-long nil
           cur-long-id long-id
           carry true
           cur-long-vec long-vec]
      (cond
       (and (= i-bit cell-width) carry) orig
       (or (= i-bit cell-width) (not carry)) (cell-array. (if-not new-long cur-long-vec
                                                                  (assoc cur-long-vec cur-long-id new-long))
                                                          cell-width num-cells)
       carry (let [new-long (or new-long cur-long)
                   is-set (bit-test cur-long offset)
                   new-new-long (if is-set (bit-clear new-long offset) (bit-set new-long offset))
                   [new-offset new-cur-long-id new-longs new-long-vec new-new-new-long]
                   (let [x (inc offset)]
                     (if (< x blk-width)
                       [x cur-long-id cur-longs cur-long-vec new-new-long]
                       [0 (inc cur-long-id) rest-of-longs (assoc cur-long-vec cur-long-id new-new-long) nil]))]
               (recur (inc i-bit) new-offset new-longs new-new-new-long new-cur-long-id is-set new-long-vec))
       :else (throw "error")))))

(defn long-vec [{:keys [num-cells] :as cellarray}] (mapv #(get-cell cellarray %) (range num-cells)))

(defn make-cell-array [^long cell-width ^long num-cells]
  (let [num-bits-long 64
        num-bits (* cell-width num-cells)
        long-vec-length (let [x (quot num-bits num-bits-long)]
                          (if (= 0 (mod num-bits num-bits-long))
                            x (inc x)))]
    (->cell-array (apply vector-of :long (repeat long-vec-length 0)) cell-width num-cells)))

(defn hash64shift [^long x]
  (let [+ unchecked-add
        - unchecked-subtract
        << bit-shift-left
        >>> bit-shift-right
        x (- (<< x 21) (+ x 1))
        x (bit-xor x (>>> x 24))
        x (+ (+ x (<< x 3)) (<< x 8))
        x (bit-xor x (>>> x 14))
        x (+ (+ x (<< x 2)) (<< x 4))
        x (bit-xor x (>>> x 28))
        x (+ x (<< x 31))]
    x))

(let [log-2 (Math/log 2.0)
      sqr-log-2 (* log-2 log-2)]
 (defn get-num-optimal-hash-funcs [num-cells capacity]
   (long (* log-2 (/ num-cells capacity))))
 (defn get-num-cells-with-optimal-hashes [capacity error-rate]
   "assuming optimum number of hash-functions"
   (long (/ (* capacity (- (Math/log error-rate))) sqr-log-2)))
 (defn get-error-rate [num-cells num-stored num-hash-funcs]
   (Math/pow (double (- 1.0 (Math/exp (- (double (/ (* num-hash-funcs num-stored) num-cells))))))
             (double num-hash-funcs)))
 (defn get-error-rate-with-optimal-hash-funcs [num-cells num-stored]
   (let [num-optimal-hash-funcs (get-num-optimal-hash-funcs num-cells num-stored)]
     (get-error-rate num-cells num-stored num-optimal-hash-funcs))))
(defn lazy-primes3 []
  (letfn [(enqueue [sieve n step]
            (let [m (+ n step)]
              (if (sieve m)
                (recur sieve m step)
                (assoc sieve m step))))
          (next-sieve [sieve candidate]
            (if-let [step (sieve candidate)]
              (-> sieve
                  (dissoc candidate)
                  (enqueue candidate step))
              (enqueue sieve candidate (+ candidate candidate))))
          (next-primes [sieve candidate]
            (if (sieve candidate)
              (recur (next-sieve sieve candidate) (+ candidate 2))
              (cons candidate
                    (lazy-seq (next-primes (next-sieve sieve candidate)
                                           (+ candidate 2))))))]
    (cons 2 (lazy-seq (next-primes {} 3)))))

(defn get-cell-width [^long threshold]
  (loop [width 0 cur-max 1]
    (if-not (< cur-max threshold) width
            (recur (inc width) (bit-shift-left cur-max 1)))))
#_ (mod (hash64shift 10) 78234)
(def hash-seeds (apply vector-of :long (take 1000 (lazy-primes3))))
(defrecord bloom-filter-core [^cell-array cells ^long num-stored num-hash-funcs desired-error-rate desired-capacity])

(defn positions [num-cells num-hash-funcs {:keys [positions hashes elem-hash elem] :as pos-info}]
  (if positions pos-info
      (if hashes (assoc pos-info :positions (mapv #(mod % num-cells) hashes))
          (if elem-hash (recur num-cells num-hash-funcs (assoc pos-info :hashes (mapv #(hash-combine elem-hash %)
                                                                                      (subvec hash-seeds 0 num-hash-funcs))))
              (if elem (recur num-cells num-hash-funcs (assoc pos-info :elem-hash (hash64shift elem)))
                  (throw "please atleast pass the element for which to compute cell positions"))))))

(defn bloom-filter-core-inc-count [{:keys [cells seeds] :as bf} ^long elem]
  (reduce (fn [cbf pos] (inc-cell cbf pos)) bf
          (positions (:num-cells cells) seeds elem)))

(defn bloom-filter-core-get-count
  ([{{:keys [cell-width num-cells]} :cells :keys [seeds cells]} & {:keys [hash-positions hashes elem-hash elem]}]
     (let [hash-positions (or hash-positions
                              (mapv #(mod % num-cells)
                                    (or hashes
                                        (mapv (let [h (or elem-hash (hash64shift elem))]
                                                #(hash-combine h %)) seeds))))]
       (loop [[pos & rest-of-poses] hash-positions
              min-val (bit-shift-left 1 cell-width)]
         (if-not pos min-val
                 (let [cnt (get-cell cells pos)]
                   (if (= 0 cnt) 0
                       (recur rest-of-poses (min min-val cnt)))))))))

(defn bloom-filter-core-error-rate [{{:keys [num-cells]} :cells :keys [num-stored seeds]}]
  "is this right?"
  (get-error-rate num-cells num-stored (count seeds)))

(defn make-bloom-filter-core [& {:keys [cell-width desired-capacity desired-error-rate]
                                 :or {cell-width 4 desired-error-rate 0.01 desired-capacity 10000}}]
  (let [num-cells (get-num-cells-with-optimal-hashes desired-capacity desired-error-rate)
        num-hash-funcs (get-num-optimal-hash-funcs num-cells desired-capacity)]
    (->bloom-filter-core (make-cell-array cell-width num-cells) 0
                         (vec (take num-hash-funcs hash-seeds)))))

(defrecord expanding-bloom-filter [bfchain cell-width
                                   next-bloom-filter-capacity
                                   next-bloom-filter-error-rate                                   
                                   error-rate-ratio capacity-growth-rate])

(defn make-expanding-bloom-filter [& {:keys [initial-capacity cell-width desired-effective-error-rate
                                             error-rate-ratio capacity-growth-rate]
                                      :or {desired-effective-error-rate 0.01
                                           error-rate-ratio 0.5
                                           cell-width 1
                                           capacity-growth-rate 10
                                           initial-capacity 10000}}]
  (let [individual-bloom-filter-error-rate (* desired-effective-error-rate (- 1.0 error-rate-ratio))]
    (->expanding-bloom-filter [(make-bloom-filter-core :cell-width cell-width
                                                       :desired-capacity initial-capacity
                                                       :desired-error-rate individual-bloom-filter-error-rate)]
                              cell-width error-rate-ratio capacity-growth-rate)))

(defn expanding-bloom-filter-inc-count [{:keys [bfchain cell-width
                                                error-rate-ratio capacity-growth-rate]
                                         :as expanding-bf} & {:as pos-info}]
  (let [{:keys [desired-capacity desired-error-rate num-stored] :as bf} (first (rseq bfchain))]
    (if (< num-stored desired-capacity)
      (assoc-in expanding-bf [:bfchain (count bfchain)] (bloom-filter-core-inc-count bf pos-info))
      (update-in (subvec expanding-bf 1) [:bfchain] conj
                 (bloom-filter-core-inc-count
                  (make-bloom-filter-core :cell-width cell-width
                                          :desired-error-rate (* error-rate-ratio desired-error-rate)
                                          :desired-capacity (* capacity-growth-rate desired-capacity)) pos-info)))))

(defn expanding-bloom-filter-get-count [{:keys [bfchain] :as expanding-bf} & {:as pos-info}] ; can optimize
  (reduce #(+ %1 (bloom-filter-core-get-count %2 pos-info)) 0 bfchain))

(defrecord aging-bloom-filter [bfchain num-aging-steps seeds num-cells expiry-age time-to-next-aging-step])

(defn make-aging-bloom-filter [& {:keys [cell-width error-rate num-aging-steps expiry-age]
                                  :or {cell-width 3 error-rate 0.01 num-aging-steps 10 expiry-age 10000}}]
  (let [{{:keys [num-cells]} :cells :keys [seeds] :as empty-bf} (make-bloom-filter-core :cell-width cell-width
                                                                                        :desired-capacity (/ expiry-age num-aging-steps)
                                                                                        :desired-error-rate error-rate)
        bfchain (vec (repeat num-aging-steps empty-bf))]
    (->aging-bloom-filter bfchain num-aging-steps seeds num-cells expiry-age (/ expiry-age num-aging-steps))))

(defn aged-count [{:keys [bfchain seeds num-cells expiry-age time-to-next-aging-step] :as aged-bf} elem]
  (let [hash-positions (positions num-cells seeds elem)]
    (reduce #(+ %1 (bloom-filter-core-get-count %2 :hash-positions hash-positions)) 0 bfchain)))

(defn aged-inc [{:keys [bfchain time-to-next-aging-step expiry-age num-aging-steps] :as aged-bf} pos-info]
  (let [last-bf (first (rseq bfchain))
        new-last-bf (bloom-filter-core-inc-count last-bf pos-info)]
    (if (= time-to-next-aging-step 1)
      (-> aged-bf
          (assoc :bfchain (conj (subvec bfchain 1) new-last-bf))
          (assoc :time-to-next-aging-step (/ expiry-age num-aging-steps)))
      (-> aged-bf
          (assoc-in [:bfchain (dec num-aging-steps)] new-last-bf)
          (update-in [:time-to-next-aging-step] dec)))))



(defrecord commonality-bloom-filter [aged-bf common-elems-membership-bloom-filter commonality-threshold])

(defn make-commonality-bloom-filter [& {:keys [commonality-threshold error-rate initial-capacity num-aging-steps expiry-age]
                                        :or {commonality-threshold 4 error-rate 0.01 num-aging-steps 10
                                             initial-capacity 100000 expiry-age 10000}}]
  (let [cell-width (get-cell-width commonality-threshold)]
   (->commonality-bloom-filter (make-aging-bloom-filter :cell-width cell-width :error-rate error-rate
                                                        :expiry-age expiry-age :num-aging-steps num-aging-steps)
                               (make-expanding-bloom-filter :initial-capacity initial-capacity :cell-width 1
                                                            :desired-effective-error-rate error-rate))))

(defn is-common [{:keys [common-elems-membership-bloom-filter]} pos-info]
  (= 0 (expanding-bloom-filter-inc-count common-elems-membership-bloom-filter pos-info)))

(defn commonality-add [{:as cbf :keys [aged-bf common-elems-membership-bloom-filter commonality-threshold]}
                       {:keys [elem] :as pos-info}]
  (if-not (is-common cbf pos-info)
    (let [cbf-1 (update-in cbf [:aged-bf] aged-inc pos-info)
          n (aged-count cbf-1 pos-info)]
      (if (>= n commonality-threshold)
        (update-in cbf-1 [:common-elems-membership-bloom-filter] #(expanding-bloom-filter-inc-count % pos-info))
        cbf-1))))


#_(map long-vec (take 3 (iterate (fn [x] (reduce (fn [old-s i] (inc-cell old-s i)) x (range 4))) s)))
#_(map long-vec (take 3 (iterate #(inc-cell % 0) s)))

#_ (let [x (make-cell-array 4 30)]
     (long-vec x))
#_ ()

#_ ()
#_ (defprotocol )