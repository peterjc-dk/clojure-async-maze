(ns maze.swing
  (:require [clojure.core.async :as as]
            [clojure.java.io :as io]
            [seesaw.core :as saw]
            [seesaw.icon :as saw-icon]
            [maze.keys :as keys]
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
         :background "#fff"
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
                                (keys/key-pressed-event-2-key-pressed-map e))))]
    (saw/listen f :key-pressed key-handler)
    (saw/show! f)
    (as/go
     (let [[v ch] (as/alts! [quit-chan])]
       (saw/hide! f)
       (saw/dispose! f)
       (log/debug "GUI Frame disposed")))
    e-chan))

(def you-are-here-dot (saw-icon/icon
                       (io/resource "you-are-here-dot.png")))

(def you-where-here (saw-icon/icon
                     (io/resource "dot.png")))

(defn scale-icon-to-label
  "given a label and a icon,
return a icon scaled to the size of the label"
  [label icon]
  (let [img (.getImage icon)
        size (saw/config label :size)
        [w h] [(max 10 (.width size))
               (max 10 (.height size))]
        smooth java.awt.Image/SCALE_SMOOTH
        scaled-img (.getScaledInstance img w h smooth)]
    (saw-icon/icon scaled-img)))

(defn change-label
  "Given a label change it"
  [label action]
  (cond (= action :enter)
        (saw/config! label
                     :icon (scale-icon-to-label
                            label
                            you-are-here-dot)
                     :background :white)
        (= action :leave)
        (saw/config! label
                     :icon (scale-icon-to-label
                            label
                            you-where-here)
                     :background :white)
        (= action :goal)
        (saw/config! label
                     :icon nil
                     :background :blue)
        (= action :init)
        (saw/config! label
                     :icon nil
                     :background :white)))

(defn switch-state
  "Given old and new state update labels"
  [labels old-state new-state]
  (let [
        leave-label (nth labels old-state)
        enter-label (nth labels new-state)]
    (change-label leave-label :leave)
    (change-label enter-label :enter)))

(defn change-gui
  "Given the labels and a chan with events update gui"
  [labels in-chan quit-chan]
  (let [_ (log/debug "GUI handler START")
        states (set (range (count labels)))
        _ (switch-state labels 0 0)
        _ (change-label (last labels) :goal)]
    (as/go
     (loop []
       (let [[v ch] (as/alts! [quit-chan in-chan])]
         (cond (= ch quit-chan)
               (log/info {:agent :change-gui
                          :action :stop
                          :allert "Stop change GUI go-loop"})
               :else
               (let [[old-state new-state] v]
                 (log/info {:agent :change-gui
                            :action [old-state new-state]})
                 (when (contains? states new-state)
                   (switch-state labels old-state new-state))
                 (recur))))))))

;:param-string (.paramString event)
