(ns maze.agents.arrow
  (:require [maze.state :as state]
            [logging.core :as log]
            [schema.core :as s]
            [clojure.core.async :as as]))

(defn arrow-to-state
  "given a action chan return next state id"
  [in-chan timetick quit-chan maze start-state agent-name]
  (let [out-chan (as/chan)
        _ (log/debug "Start action to state handler")]
    (as/go (as/>! out-chan [agent-name start-state start-state]))
    (as/go (loop [state start-state]
             (let [timeout (as/timeout timetick)
                   [v ch] (as/alts! [quit-chan in-chan timeout])]
               (when-not (= ch quit-chan)
                 (recur (condp = ch

                          in-chan
                          (try
                            (let [action (s/validate (s/enum :left :up :down :right) v)
                                  next-state (state/get-new-position state action maze)]
                              (log/info {:agent agent-name
                                         :action action
                                         :state next-state})
                              (as/>! out-chan [agent-name state next-state])
                              next-state)
                            (catch Exception e
                              (do
                                (as/>! out-chan [agent-name state state])
                                state)))

                          timeout
                              (do
                                (as/>! out-chan [agent-name state state])
                                state)))))))
    out-chan))
