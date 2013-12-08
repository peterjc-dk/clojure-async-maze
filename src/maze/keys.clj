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

(defn key-pressed-map-2-keyword
  "Given a map representing a event, extract keyword"
  [m]
  (-> (:key-text m)
      (.toLowerCase)
      keyword))

(defn split-keyword-2-chans
  "Use filter on in chan to relay to out channels"
  [in-chan keyword-set-list]
  (let [cs (repeatedly (count keyword-set-list) as/chan)
        cs-keyword-map (map (fn [keys ch] {:keys keys :chan ch}) keyword-set-list cs)]
    (as/go (while true
             (let [in-val (as/<! in-chan)]
               (doseq [{keys :keys out-chan :chan} cs-keyword-map]
                 (when (contains? keys in-val)
                  (as/>! out-chan in-val))))))
    cs))
