(ns maze.core
  (:require [maze.keys :as keys]
            [maze.generate :as generate]
            [maze.state :as state]
            [maze.swing.swing :as swing-gui]
            [maze.swing.swing_pop :as swing-pop]
            [maze.swing.draw_maze :as draw-maze]
            [maze.util :as util]
            [maze.agents.left :as agent-left]
            [maze.agents.arrow :as agent-arrow]
            [maze.agents.mouse_walker :as agent-mouse]
            [clojure.core.async :as as]
            [clojure.core.async.lab :as as-lab])
  (:gen-class))

(defn wrap-setup-gui
  "Wrapper for the GUI setup fn"
  [maze labels]
  (fn [q-ch] (swing-gui/setup-gui maze labels q-ch)))

(defn wrap-key-keyword
  "Wrapper for key events to keywords"
  []
  (fn [in-ch] (util/fn-c
              in-ch
              keys/key-pressed-map-2-keyword)))

(defn wrap-draw-maze
  "Wrapper for maze drawing"
    [maze labels goal-index day-or-night]
    (fn [q-ch] (when (= day-or-night :day)
                (draw-maze/draw-maze
                 labels
                 maze
                 q-ch
                 2
                 goal-index))))

(defn wrap-key-split
  "Wrapper for spilting keyword in channels fn"
  [list-of-sets]
  (fn [in-ch] (keys/split-keyword-2-chans
              in-ch
              list-of-sets)))

(defn wrap-arrow-to-state
  "Wrapper for arrow to state fn"
  [maze start-state timetick]
  (fn [in-ch1 in-ch2 q-ch] (agent-arrow/arrow-to-state
                           in-ch1
                           in-ch2
                           q-ch
                           maze
                           start-state
                           :user-walker
                           timetick)))

(defn wrap-keep-left
  "Wrapper for the keep left fn"
  [maze start-state timetick]
  (fn [q-ch] (agent-left/keep-to-the-left
             q-ch maze
             start-state
             timetick)))

(defn wrap-change-gui
  "Wrapper for the change GUI ch"
  [labels goal-index day-or-night]
  (fn [in-ch q-ch]
    (swing-gui/change-gui
     labels
     in-ch
     q-ch
     goal-index
     day-or-night)))

(defn hook-channels-together
  [setup
   key->keyword
   draw-maze
   key-split
   arrow->state
   keep-left->state
   state->gui]
  (let [[q1 q2 q3 q4 q5] (repeatedly 5 as/chan)
        quit-bc-ch (as-lab/broadcast q1 q2 q3 q4 q5)
        [key-ch mouse-ch] (setup q1)
        keyword-ch (key->keyword key-ch)
        _ (draw-maze q2)
        [arrow-ch quit-key-ch] (key-split keyword-ch)
        st1-ch (arrow->state arrow-ch mouse-ch q3)
        st2-ch (keep-left->state q4)
        st-all-ch (util/fan-in [st1-ch st2-ch])]
    (as/go (as/>! quit-bc-ch
                  (as/<! (state->gui st-all-ch q5))))
    (as/go (as/>! quit-bc-ch (as/<! quit-key-ch)))
    (as/<!! q2)))

(defn inner-main [columns rows day-or-night]
  (let [timetick 100
        arrows #{:up :down :left :right}
        quit-keyword #{:q}
        start-index 0
        goal-index (dec (* columns rows))
        maze (generate/generate-maze [columns rows])
        labels (map swing-gui/make-maze-tile (:board maze))]
    (hook-channels-together
     (wrap-setup-gui maze labels)
     (wrap-key-keyword)
     (wrap-draw-maze maze labels goal-index day-or-night)
     (wrap-key-split [arrows quit-keyword])
     (wrap-arrow-to-state maze start-index timetick)
     (wrap-keep-left maze start-index timetick)
     (wrap-change-gui labels goal-index day-or-night))))

(defn -main
  "Main outer loop"
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (loop [done :no default-columns 10 default-rows 10]
    (when (contains? #{:no} done)
      (let [{day-or-night :day-or-night
            columns :columns
            rows :rows} (swing-pop/welcome-pop default-columns default-rows)]
        (recur (inner-main columns rows day-or-night)
               (+ columns (rand-int columns))
               (+ rows (rand-int rows)))))))
