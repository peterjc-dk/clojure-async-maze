(ns maze.keys
  (:require [clojure.core.async :as as]
            [logging.core :as log])
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
    (log/debug (str "Start key filter" keys))
    (as/go (while true
             (let [in-val (as/<! in-chan)
                   key-text (:key-text in-val)]
               (when (contains? keys key-text)
                 (log/info (keyword (.toLowerCase key-text)))
                 (as/>! out-chan (keyword (.toLowerCase key-text)))))))
    out-chan))
