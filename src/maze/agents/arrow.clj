(ns maze.agents.arrow
  (:require [maze.state :as state]
            [logging.core :as log]
            [clojure.core.async :as as]))

(defn arrow-to-state
  "given a action chan return next state id"
  [in-chan quit-chan maze start-state day-or-night]
  (let [out-chan (as/chan)
        day-or-night-str (name day-or-night)
        agent-name (keyword (str "arrow-" day-or-night-str "-walker") )
        _ (log/debug "Start action to state handler")
        _ (as/go (as/>! out-chan [agent-name 0 0]))]
    (as/go (loop [state start-state]
             (let [[v ch] (as/alts! [quit-chan in-chan])]
               (cond (= ch in-chan)
                     (let [next-state (state/get-new-position state v maze)]
                       (log/info {:agent agent-name
                                  :action v
                                  :state next-state})
                       (as/>! out-chan [agent-name state next-state])
                       (recur next-state))
                     (= ch quit-chan)
                     (do (log/info "stopped action handler"))))))
    out-chan))
