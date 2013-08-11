(ns maze.agents.mouse_walker
  (:require [maze.state :as state]
            [logging.core :as log]
            [clojure.core.async :as as]))

(defn mouse-to-state
  "given a mouse action chan return next state id"
  [in-chan quit-chan maze start-state day-or-night]
  (let [out-chan (as/chan)
        day-or-night-str (name day-or-night)
        agent-name (keyword (str "arrow-"  day-or-night-str "-walker") )]
    (log/debug "Start Mouse action to state handler")
    (as/go (as/>! out-chan [agent-name 0 0]))
    (as/go (loop [state start-state]
             (let [[v ch] (as/alts! [quit-chan in-chan])
                   _ (println "Mouse value: " v)]
               (cond (= ch in-chan)
                     (recur
                      (if-let [a-nb (state/next-to-each-other state (:tile v) maze)]
                        (let [[action position] a-nb
                              _ (println "a-nb " a-nb)
                              next-state (state/get-new-position state action maze)
                              _ (println "next state" next-state)]
                          (log/info {:agent agent-name
                                     :action action
                                     :state next-state})
                          (as/>! out-chan [agent-name state next-state])
                          next-state)
                        ;; else
                        (do (println "else") state)))
                     (= ch quit-chan)
                     (do (log/info "stopped mouse action handler"))))))
    out-chan))
