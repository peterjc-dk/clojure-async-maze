(ns maze.agents.random
  (:require [maze.state :as state]
            [logging.core :as log]
            [clojure.core.async :as as]))

(defn random-walk
  "Create random action and send state array to the GUI"
  [quit-chan maze star-state timeout]
  (let [out-chan (as/chan)
        _ (log/debug "Start Random action to state handler")]
    (as/go (loop [state star-state]
             (let [[v ch] (as/alts! [quit-chan (as/timeout timeout)])]
               (cond (= ch quit-chan)
                     (log/info "stopped action handler")
                     :else
                     (let [rand-action (rand-nth [:up :down :right :left])
                           next-state (state/get-new-position
                                       state rand-action maze)]
                       (log/info {:agent :ramdom-walk
                                  :action rand-action
                                  :state next-state})
                       (as/>! out-chan [:ramdom-walk state next-state])
                       (recur next-state))))))
    out-chan))
