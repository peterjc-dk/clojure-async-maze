(ns maze.swing.swing
  (:require [clojure.core.async :as as]
            [clojure.string :as str]
            [seesaw.core :as saw]
            [seesaw.icon :as saw-icon]
            [maze.keys :as keys]
            [maze.swing.swing_label :as swing-label]
            [maze.swing.swing_pop :as swing-pop]))

(defn init-tile
  "init the given label"
  [labels tile-index]
  (let [label (nth labels tile-index)
        bg-color (seesaw.color/get-rgba (saw/config label :background))
        black-rgba [0 0 0 255]]
    (when (= bg-color black-rgba)
      (swing-label/change-label-config label nil :black :white))))

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

(defn label-mouse-handler
  "Create handler fn to a label"
  [ch index]
  (fn [e] (as/go
          (as/>! ch {:event :mouse-clicked
                     :tile index} ))))

(defn setup-gui
  "Setup the swing/seasaw gui"
  [maze labels quit-chan]
  (let [key-chan (as/chan)
        mouse-chan (as/chan)
        columns (:columns maze)
        rows (:rows maze)
        f (make-frame "The Maze" columns rows (vec labels))
        key-handler (fn [e] (as/go
                            (as/>! key-chan
                                   (keys/key-pressed-event-2-key-pressed-map e))))]
    (saw/listen f :key-pressed key-handler)

    (let [idv (map vector (iterate inc 0) labels)]
      (doseq [[index label] idv]
        (saw/listen label :mouse-clicked
                    (label-mouse-handler mouse-chan index))))
    (saw/show! f)
    (as/go
     (let [[v ch] (as/alts! [quit-chan])]
       (saw/hide! f)
       (saw/dispose! f)))
    [key-chan mouse-chan]))

(defn change-gui
  "Given the labels and a chan with events update gui"
  [labels in-chan quit-chan goal-index day-or-night]
  (let [states (set (range (count labels)))]
    (swing-label/set-goal labels goal-index)
    (as/go
     (loop []
       (let [[v ch] (as/alts! [quit-chan in-chan])]
         (condp = ch
           quit-chan
           :stop

           in-chan
           (let [[agent old-state new-state] v]
             (when (contains? states new-state)
               (swing-label/switch-state labels agent old-state new-state day-or-night)
               (let [we-are-there (swing-pop/are-we-there-yet? new-state goal-index)]
                 (if-not we-are-there
                   (recur)
                   we-are-there))))))))))
