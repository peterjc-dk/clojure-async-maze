(ns maze.core
  (:require [maze.keys :as keys]
            [maze.generate :as generate]
            [maze.state :as state]
            [maze.swing :as swing-gui]
            [maze.util :as util]
            [maze.agents.left :as agent-left]
            [maze.agents.arrow :as agent-arrow]
            [maze.agents.random :as agent-random]
            [logging.core :as log]
            [clojure.core.async :as as]
            [clojure.core.async.lab :as as-lab])
  (:gen-class))

(defn draw-maze
  "Create agent that illustrates generation of the maze the  and send state array to the GUI"
  [quit-chan maze timeout]
  (let [out-chan (as/chan)
        _ (log/debug "Start Go Left to state handler")
        [c r] [(:columns maze) (:rows maze)]
        start-state (state/position-to-index (first (:path maze)) [c r])
        _ (println "Start state " start-state)]
    (as/go (loop [state start-state path (rest (:path maze))]
             (let [[v ch] (as/alts! [quit-chan (as/timeout timeout)])]
               (cond (or (empty? path) (= ch quit-chan))
                     (log/info {:agent :generate-maze
                                :action :stop
                                :allert "stopped action handler"})
                     :else
                     (let [new-state (state/position-to-index (first path) [c r])]
                       (log/info {:agent :generate-maze
                                  :state new-state})
                       (as/>! out-chan [state new-state])
                       (recur new-state (rest path)))))))
    out-chan))


(defn inner-main [columns rows day-or-night]
  (let [arrows #{"Up" "Down" "Left" "Right"}
        quit-key #{"q" "Q"}
        goal-index (dec (* columns rows))
        [q1-out q2-out q3-out q4-out q5-out] [(as/chan) (as/chan) (as/chan) (as/chan) (as/chan)]
        quit-bc-ch-out (as-lab/broadcast q1-out q2-out q3-out q4-out q5-out)

        _ (log/debug (str "Generate Maze " columns "x" rows))
        maze (generate/generate-maze [columns rows])
        _ (log/debug "Show Swing GUI")
        labels (map swing-gui/make-maze-tile (:board maze))

        key-ch-in (swing-gui/setup-gui maze labels q1-out)

        _ (when (= day-or-night :day)
            (swing-gui/draw-maze labels maze q3-out 2 goal-index))
        [arrow-ch-in quit-key-ch-in] (keys/split-key-2-chans key-ch-in [arrows quit-key])
        st1-ch-in (agent-arrow/arrow-to-state arrow-ch-in q1-out maze 0 day-or-night)
        ;st2-ch-in (draw-maze q2-out maze  100)
        ;st2-ch-in (agent-random/random-walk q2-out maze (maze :columns) 100)
        ;st3-ch-in (agent-left/keep-to-the-left q3-out maze 20)
        ;st-all-ch-in (util/fan-in [st1-ch-in st2-ch-in st3-ch-in])
        st-all-ch-in (util/fan-in [st1-ch-in])
        ]
    (as/go (as/>! quit-bc-ch-out
                   (as/<!
                    (swing-gui/change-gui labels
                                          st-all-ch-in
                                          q5-out
                                          goal-index))))
    (as/go (as/>! quit-bc-ch-out (as/<! quit-key-ch-in)))
    (log/debug "Main setup done")
    (as/<!! q3-out)))


;(System/setProperty "apple.laf.useScreenMenuBar" "true")
;(System/setProperty "com.apple.mrj.application.apple.menu.about.name" "TestHest")

;; TODO fix this
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (loop [done :no default-columns 10 default-rows 10]
    (when (contains? #{:no} done)
      (let [{day-or-night :day-or-night
            columns :columns
            rows :rows} (swing-gui/welcome-pop default-columns default-rows)]
        (recur (inner-main columns rows day-or-night)
               (+ columns (rand-int columns))
               (+ rows (rand-int rows)))))))
