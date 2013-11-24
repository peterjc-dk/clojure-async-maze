(ns maze.agents.left
  (:require
   [maze.state :as state]
   [logging.core :as log]
   [clojure.core.async :as as]))

(defn get-next-action
  "find next action for the go rigth agent"
  [old-state new-state action]
  (let [action-wheel {:up :right
                      :right :down
                      :down :left
                      :left :up}
        action-reverse {:up :down
                        :right :left
                        :down :up
                        :left :right}]
    (if (= old-state new-state)
      (action-wheel action)
      (action-wheel (action-reverse action)))))

(defn keep-to-the-left
  "Create agent that do go left action (relativly) and send state array to the GUI"
  [quit-chan maze start-state timetick]
  (let [out-chan (as/chan)
        _ (log/debug "Start Go Left to state handler")]
    (as/go (loop [state start-state action :up]
             (let [timeout (as/timeout timetick)
                   [v ch] (as/alts! [quit-chan timeout])]
               (condp = ch

                 quit-chan
                 (as/>! quit-chan {:agent :left-walk
                            :action :stop
                            :allert "stopped action handler"})

                 timeout
                 (let [new-state (state/get-new-position state action maze)
                       next-action (get-next-action state new-state action)]
                   (as/>! out-chan [:left-walk state new-state])
                   (recur new-state next-action))))))
    out-chan))
