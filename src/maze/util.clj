(ns maze.util
  (:require
   [logging.core :as log]
   [clojure.core.async :as as]))

(defn sink-chan
  "consume and a chan and listen to quit chan"
  [in-chan quit-chan]
  (as/go (loop []
        (let [[v ch] (as/alts! [quit-chan in-chan])]
          (cond (= ch in-chan)
                (do
                  (log/info v)
                  (recur))
                (= ch quit-chan)
                (do (log/info "sink stopped")))))))

(defn fan-in [ins]
  (let [c (as/chan)]
    (as/go (while true
          (let [[x] (as/alts! ins)]
            (as/>! c x))))
    c))

(defn fan-out [in cs-or-n]
  (let [cs (if (number? cs-or-n)
             (repeatedly cs-or-n as/chan)
             cs-or-n)]
    (as/go (while true
          (let [x (as/<! in)
                outs (map #(vector % x) cs)]
            (as/alts! outs))))
    cs))

(defn in-out [in out]
  (as/go
   (while true
     (let [x (as/<! in)]
       (as/>! out x)))))

(defn chan-2-lazy-seq
  "chan to lazy seq"
  [in-chan]
  (fn lz [] (cons (as/<!! in-chan) (lazy-seq (lz)))))

(defn ch-2-lazy
  "chan to lazy seq"
  [in-chan]
  (cons (as/<!! in-chan) (lazy-seq (ch-2-lazy in-chan))))

(defn ch-2-lazy-timeout
  "chan to lazy seq"
  [in-chan timeout]
  (cons (first (as/alts!! [in-chan (as/timeout timeout)]))
        (lazy-seq (ch-2-lazy-timeout in-chan timeout))))

(defn sq-2-chan [sq]
  (let [c (as/chan)]
    (as/go (loop [s sq]
          (if s
            (do (as/>! c (first s))
                (recur (rest s))) (as/close! c))))
    c))
