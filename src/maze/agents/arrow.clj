(ns maze.agents.arrow
  (:require [maze.state :as state]
            [schema.core :as s]
            [clojure.core.async :as as]))

(defn get-next-state [state a maze]
  (try
    (let [action (s/validate (s/enum :left :up :down :right) a)]
      (state/get-new-position state action maze))
  (catch Exception e
    state)))

(defn arrow-to-state
  "given a action chan return next state id"
  [in-chan quit-chan maze start-state agent-name timetick]
  (let [out-chan (as/chan)]
    (as/go (as/>! out-chan [agent-name start-state start-state]))
    (as/go (loop [state start-state]
             (let [timeout (as/timeout timetick)
                   [v ch] (as/alts! [quit-chan in-chan timeout])]
               (condp = ch

                 in-chan
                 (let [next-state (get-next-state state v maze)]
                   (as/>! out-chan [agent-name state next-state])
                   (recur next-state))

                 timeout
                 (do
                   (as/>! out-chan [agent-name state state])
                   (recur state))

                 quit-chan
                 (println (str "Stop agent " agent-name))))))
    out-chan))
