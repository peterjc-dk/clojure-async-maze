(ns maze.keys
  (:require [clojure.core.async :as as]
            [clojure.core.async.lab :as as-lab])
  (:import (java.awt.event KeyEvent)))

(defn key-pressed-event-2-key-pressed-map
  "Convert a java KeyEvent object to at clojure map"
  [event]
  {:key-code (.getKeyCode event)
   :key-char (.getKeyChar event)
   :key-text (KeyEvent/getKeyText (.getKeyCode event))
   :when (.getWhen event)
   :is-action-key (.isActionKey event)})

(defn filter-key-map-chan
  "Use filter on in chan to out chan"
  [in-chan keys]
  (let [out-chan (as/chan)]
    (as/go (while true
             (let [in-val (as/<! in-chan)
                   key-text (:key-text in-val)]
               (when (contains? keys key-text)
                 (as/>! out-chan (keyword (.toLowerCase key-text)))))))
    out-chan))

(defn fan-out [in cs-or-n]
  (let [cs (if (number? cs-or-n)
             (repeatedly cs-or-n as/chan)
             cs-or-n)]
    (as/go (while true
          (let [x (as/<! in)
                outs (map #(vector % x) cs)]
            (as/alts! outs))))
    cs))

(defn split-key-2-chans
  "Use filter on in chan to relay to one out chan"
  [in-chan key-set-list]
  (let [cs (repeatedly (count key-set-list) as/chan)
        cs-key-map (map (fn [keys ch] {:keys keys :chan ch}) key-set-list cs)]
    (as/go (while true
             (let [in-val (as/<! in-chan)
                   key-text (:key-text in-val)]
               (doseq [{keys :keys out-chan :chan} cs-key-map]
                 (when (contains? keys key-text)
                  (as/>! out-chan (keyword (.toLowerCase key-text))))))))
    cs))
