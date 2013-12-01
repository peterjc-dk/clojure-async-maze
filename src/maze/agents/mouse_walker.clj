(ns maze.agents.mouse_walker
  (:require [maze.state :as state]
            [clojure.core.async :as as]))

(defn mouse-to-state
  "given a mouse action chan return next state id"
  [in-chan quit-chan maze start-state day-or-night]
  (let [out-chan (as/chan)
        day-or-night-str (name day-or-night)
        agent-name (keyword (str "arrow-"  day-or-night-str "-walker") )]
    (as/go (as/>! out-chan [agent-name 0 0]))
    (as/go (loop [state start-state]
             (let [[v ch] (as/alts! [quit-chan in-chan])]
               (cond (= ch in-chan)
                     (recur
                      (if-let [a-nb (state/next-to-each-other state (:tile v) maze)]
                        (let [[action position] a-nb
                              next-state (state/get-new-position state action maze)]
                          (as/>! out-chan [agent-name state next-state])
                          next-state)
                        ;; else
                        state))
                     (= ch quit-chan)
                     (do (println "stopped mouse action handler"))))))
    out-chan))
