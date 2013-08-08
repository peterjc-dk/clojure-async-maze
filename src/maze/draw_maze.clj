(ns maze.draw_maze
  (:require [clojure.core.async :as as]
            [seesaw.core :as saw]
            [seesaw.icon :as saw-icon]
            [maze.state :as state]
            [maze.swing_label :as swing-label]
            [logging.core :as log]))

(defn init-tile
  "init the given label"
  [labels tile-index]
  (let [label (nth labels tile-index)
        bg-color (seesaw.color/get-rgba (saw/config label :background))
        black-rgba [0 0 0 255]]
    (when (= bg-color black-rgba)
      (swing-label/change-label label :arrow-day-walker :init))))

(defn draw-maze
  "Given the labels and a chan with events update gui"
  [labels maze quit-chan timeout goal-index]
  (let [_ (log/debug "Draw Maze START")
        states (-> labels count
                   dec range set)
        [c r] [(:columns maze) (:rows maze)]
        start-state (state/position-to-index (first (:path maze)) [c r])
        _ (init-tile labels start-state)]
    (as/go
     (loop [state start-state path (rest (:path maze))]
       (let [[v ch] (as/alts! [quit-chan (as/timeout timeout)])]
         (cond (or (empty? path) (= ch quit-chan))
               (do
                 (log/info {:agent :draw-maze
                            :action :stop
                            :allert "Drawing maze stopped"}))
               :else
               (let [new-state (state/position-to-index (first path) [c r])]
                 (log/info {:agent :draw-maze
                            :action [state new-state]})
                 (when (contains? states new-state)
                   (init-tile labels new-state))
                 (recur new-state (rest path)))))))))
