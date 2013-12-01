(ns maze.agents.random
  (:require [maze.state :as state]
            [clojure.core.async :as as]))

(defn random-walk
  "Create random action and send state array to the GUI"
  [quit-chan maze star-state timeout]
  (let [out-chan (as/chan)]
    (as/go (loop [state star-state]
             (let [[v ch] (as/alts! [quit-chan (as/timeout timeout)])]
               (cond (= ch quit-chan)
                     (println "stopped action handler")
                     :else
                     (let [rand-action (rand-nth [:up :down :right :left])
                           next-state (state/get-new-position
                                       state rand-action maze)]
                       (as/>! out-chan [:ramdom-walk state next-state])
                       (recur next-state))))))
    out-chan))
