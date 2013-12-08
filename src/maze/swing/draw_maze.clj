(ns maze.swing.draw_maze
  (:require [clojure.core.async :as as]
            [maze.state :as state]
            [maze.swing.swing :as swing]
            [maze.swing.swing_label :as swing-label]))

(defn draw-maze
  "Given the labels and a chan with events update gui"
  [labels maze quit-chan timeout goal-index]
  (let [states (-> labels count
                   dec range set)
        [c r] [(:columns maze) (:rows maze)]
        start-state (state/position-to-index (first (:path maze)) [c r])]
    (swing/init-tile labels start-state)
    (as/go
     (loop [state start-state path (rest (:path maze))]
       (let [[v ch] (as/alts! [quit-chan (as/timeout timeout)])]
         (cond (or (empty? path) (= ch quit-chan))
               (println {:agent :draw-maze
                         :action :stop
                         :allert "Drawing maze stopped"})
               :else
               (let [new-state (state/position-to-index (first path) [c r])]
                 (when (contains? states new-state)
                   (swing/init-tile labels new-state))
                 (recur new-state (rest path)))))))))
