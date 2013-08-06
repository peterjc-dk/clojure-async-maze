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

(defn inner-main [columns rows]
  (let [arrows #{"Up" "Down" "Left" "Right"}
        quit-key #{"q" "Q"}

        [q1-out q2-out q3-out q4-out q5-out] [(as/chan) (as/chan) (as/chan) (as/chan) (as/chan)]
        quit-bc-ch-out (as-lab/broadcast q1-out q2-out q3-out q4-out q5-out)

        _ (log/debug (str "Generate Maze " columns "x" rows))
        maze (generate/generate-maze [columns rows])
        _ (log/debug "Show Swing GUI")
        labels (map swing-gui/make-maze-tile (:board maze))

        key-ch-in (swing-gui/setup-gui maze labels q1-out)

        [arrow-ch-in quit-key-ch-in] (keys/split-key-2-chans key-ch-in [arrows quit-key])
        st1-ch-in (agent-arrow/arrow-to-state arrow-ch-in q1-out maze)
        st2-ch-in (agent-random/random-walk q2-out maze (maze :columns) 100)
        ;st3-ch-in (agent-left/keep-to-the-left q3-out maze)
        ;st-all-ch-in (util/fan-in [st1-ch-in st2-ch-in st3-ch-in])
        st-all-ch-in (util/fan-in [st1-ch-in st2-ch-in])
        ]
    (swing-gui/change-gui labels st-all-ch-in q5-out)
    (as/go (as/>! quit-bc-ch-out (as/<! quit-key-ch-in)))
    (log/debug "Main done")))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (inner-main 44 33))

(comment
  (inner-main 44 25)
  (-main)
)
