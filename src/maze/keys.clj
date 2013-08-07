(ns maze.keys
  (:require [clojure.core.async :as as]
            [clojure.core.async.lab :as as-lab]
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
    (log/debug (str "Start key filter" key-set-list))
    (as/go (while true
             (let [in-val (as/<! in-chan)
                   key-text (:key-text in-val)]
               (doseq [{keys :keys out-chan :chan} cs-key-map]
                 (when (contains? keys key-text)
                  (log/info (keyword (.toLowerCase key-text)))
                  (as/>! out-chan (keyword (.toLowerCase key-text))))))))
    cs))

(defn split-keys-old
  "Given a chan of keyboard presses split in arrows an q"
  [in-keys-ch]
  (let [arrows #{"Up" "Down" "Left" "Right"}
        quit-key #{"q" "Q"}
        k1-out (as/chan)
        k2-out (as/chan)
        key-bc-ch-out (as-lab/broadcast k1-out k2-out)
        arrow-ch (filter-key-map-chan k1-out arrows)
        quit-key-ch (filter-key-map-chan k2-out quit-key)]
    ;(in-out in-keys-ch key-bc-ch-out)
    [arrow-ch quit-key-ch]))
