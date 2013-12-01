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
            [maze.agents.random :as agent-random]
            [clojure.core.async :as as]
            [clojure.core.async.lab :as as-lab])
  (:gen-class))


(defn inner-main [columns rows day-or-night]
  (let [timetick 100
        arrows #{"Up" "Down" "Left" "Right"}
        quit-key #{"q" "Q"}
        goal-index (dec (* columns rows))

        [q1-out q2-out q3-out q4-out q5-out] [(as/chan) (as/chan) (as/chan) (as/chan) (as/chan)]
        quit-bc-ch-out (as-lab/broadcast q1-out q2-out q3-out q4-out q5-out)

        maze (generate/generate-maze [columns rows])
        labels (map swing-gui/make-maze-tile (:board maze))

        key-ch-in (swing-gui/setup-gui maze labels q1-out)

        _ (when (= day-or-night :day)
            (draw-maze/draw-maze labels maze q2-out 2 goal-index))
        [arrow-ch-in quit-key-ch-in] (keys/split-key-2-chans key-ch-in [arrows quit-key])

        st1-ch-in (agent-arrow/arrow-to-state arrow-ch-in q3-out maze 0 :user-walker timetick)
        st2-ch-in (agent-left/keep-to-the-left q4-out maze 20 timetick)
        st-all-ch-in (util/fan-in [st1-ch-in st2-ch-in])
        ]
    (as/go (as/>! quit-bc-ch-out
                   (as/<!
                    (swing-gui/change-gui labels
                                          st-all-ch-in
                                          q5-out
                                          goal-index
                                          day-or-night))))
    (as/go (as/>! quit-bc-ch-out (as/<! quit-key-ch-in)))
    (as/<!! q2-out)))

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
