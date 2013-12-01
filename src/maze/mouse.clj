(ns maze.mouse
  (:require [clojure.core.async :as as]
            [clojure.core.async.lab :as as-lab]
            [seesaw.core :as saw]
            [maze.generate :as generate]
            [maze.state :as state]
            [maze.swing :as swing-gui]
            [maze.keys :as keys])

  (:import (java.awt.event KeyEvent)))

(defn key-pressed-event-2-key-pressed-map
  "Convert a java KeyEvent object to at clojure map"
  [event]
  (println "Event")
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

(defn setup-gui-3
  "Setup the swing/seasaw gui"
  [maze labels quit-chan]
  (let [e-chan (as/chan)
        columns (:columns maze)
        rows (:rows maze)
        f (swing-gui/make-frame "The Maze 2" columns rows (into [] labels))
        key-handler (fn [e] (as/go
                            (as/>! e-chan
                                   (key-pressed-event-2-key-pressed-map e))))]

    (saw/listen f :key-pressed key-handler)
    (saw/show! f)
    (as/go
     (let [[v ch] (as/alts! [quit-chan])]
       (saw/hide! f)
       (saw/dispose! f)
       (log/debug "GUI Frame disposed")))
    e-chan))

(defn tile-label-listen
  "Given a list of tile lables listen to clicks"
  [labels]
  (let [out-chan (as/chan)
        _ (println "Labels:!! " (count labels))]
    (dotimes [i (count labels)]
      (let [_ (println "OK" i)
            label (nth labels i)
            _ (println "Label" label)
            _ (println "index" i)
            _ (println "")
            mouse-handler (fn [e] (as/go
                                  (as/>! out-chan
                                         {:event :mouse-clicked
                                          :tile i})))]
        (saw/listen label :mouse-clicked mouse-handler)))
    out-chan))

(def labels (map swing-gui/make-maze-tile board))
(def out-chan (as/chan))
(tile-label-listen labels)
(for [i (range (count labels))]
      (let [_ (println "OK" i)
            label (nth labels i)
            _ (println "Label" label )
            _ (println "index" i)
            _ (println "")
            mouse-handler (fn [e] (as/go
                                  (as/>! out-chan
                                         {:event :mouse-clicked
                                          :tile i})))]
        (saw/listen label :mouse-clicked mouse-handler)))

(defn mouse-fun-2
  [columns rows]
  (let [q1-out (as/chan)
        arrows #{"Up" "Down" "Left" "Right"}
        quit-key #{"q" "Q"}
        maze (generate/generate-maze [columns rows])

        labels (map swing-gui/make-maze-tile (:board maze))

        key-ch-in (setup-gui-3 maze labels q1-out)
        mouse-chan (tile-label-listen labels)
        ;[quit-key-ch-in] (keys/split-key-2-chans key-ch-in [quit-key])
        ]
    ;(as/go (as/>! q1-out (as/<! quit-key-ch-in)))
    (as/go (while true (println (as/<! key-ch-in))))
    (as/go (while true (println (as/<! mouse-chan))))
    ))

(mouse-fun-2 10 10)

(println (generate/generate-maze [4 4]))
