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
