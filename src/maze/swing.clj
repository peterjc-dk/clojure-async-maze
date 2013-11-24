(ns maze.swing
  (:require [clojure.core.async :as as]
            [clojure.string :as str]
            [seesaw.core :as saw]
            [maze.keys :as keys]
            [maze.swing_label :as swing-label]
            [maze.swing_pop :as swing-pop]
            [logging.core :as log]))

(defn make-frame
  "create the swing grid frame given a list of labels"
  [title columns rows labels]
  (saw/frame :title title
          :size [(* columns 24) :by (* rows 24)]
          :on-close :dispose
          :content  (saw/grid-panel
                     :hgap 0 :vgap 0 :border 0
                     :columns columns
                     :rows  rows
                     :items labels )))

(defn neighbors-2-border
  "given a list of Neighbors create a border that visualize this"
  [neighbors]
  (let [neighbor-default-map {:up 2 :down 2
                              :left 2 :right 2}
        neighbor-map (reduce #(assoc %1 %2 0)
                             neighbor-default-map neighbors)]
    (seesaw.border/line-border :color :black
                               :top  (neighbor-map :up)
                               :bottom (neighbor-map :down)
                               :left (neighbor-map :left)
                               :right (neighbor-map :right))))

(defn make-maze-tile
  "Create a tile peace of the maze"
  [neighbors]
  (saw/label :text  ""
             :border (neighbors-2-border neighbors)
             :background :black
             :foreground :black))

(defn setup-gui
  "Setup the swing/seasaw gui"
  [maze labels quit-chan]
  (let [e-chan (as/chan)
        columns (:columns maze)
        rows (:rows maze)
        f (make-frame "The Maze" columns rows (into [] labels))
        key-handler (fn [e] (as/go
                            (as/>! e-chan
                                   (keys/key-pressed-event-2-key-pressed-map e))))
        mouse-handler (fn [e] (println e))]
    (saw/listen f :key-pressed key-handler)
    (saw/listen f :mouse-clicked  key-handler)
    (saw/show! f)
    (as/go
     (let [[v ch] (as/alts! [quit-chan])]
       (saw/hide! f)
       (saw/dispose! f)
       (log/debug "GUI Frame disposed")))
    e-chan))

(defn change-gui
  "Given the labels and a chan with events update gui"
  [labels in-chan quit-chan goal-index day-or-night]
  (let [_ (log/debug "GUI handler START")
        states (set (range (count labels)))
        _ (swing-label/set-goal labels goal-index)]
    (as/go
     (loop []
       (let [[v ch] (as/alts! [quit-chan in-chan])]
         (cond (= ch quit-chan)
               (do (log/info {:agent :change-gui
                              :action :stop
                              :allert "Stop change GUI go-loop"})
                   :stop)
               :else
               (let [[agent old-state new-state] v]
                 (log/info {:agent :change-gui
                            :action [agent old-state new-state]})
                 (when (contains? states new-state)
                   (swing-label/switch-state labels agent old-state new-state day-or-night)
                   (let [we-are-there (swing-pop/are-we-there-yet? new-state goal-index)]
                     (if-not we-are-there
                      (recur)
                      we-are-there))))))))))
