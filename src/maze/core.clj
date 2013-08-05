(ns maze.core
  (:require [maze.keys :as keys]
            [maze.generate :as generate]
            [maze.state :as state]
            [maze.swing :as swing-gui]
            [logging.core :as log]
            [clojure.core.async :as as]
            [clojure.core.async.lab :as as-lab])
  (:gen-class))

(defn sink-chan
  "consume and a chan and listen to quit chan"
  [in-chan quit-chan]
  (as/go (loop []
        (let [[v ch] (as/alts! [quit-chan in-chan])]
          (cond (= ch in-chan)
                (do
                  (log/info v)
                  (recur))
                (= ch quit-chan)
                (do (log/info "sink stopped")))))))

(defn action-2-next-state
  "given a action chan return next state id"
  [in-chan quit-chan maze]
  (let [out-chan (as/chan)
        _ (log/debug "Start action to state handler")]
    (as/go (loop [state 0]
             (let [[v ch] (as/alts! [quit-chan in-chan])]
               (cond (= ch in-chan)
                     (let [next-state (state/get-new-position state v maze)]
                       (log/info {:agent :arrow-walk
                                  :action v
                                  :state next-state})
                       (as/>! out-chan [state next-state])
                       (recur next-state))
                     (= ch quit-chan)
                     (do (log/info "stopped action handler"))))))
    out-chan))

(defn random-walk
  "Create randdom action and send state array to the GUI"
  [quit-chan maze]
  (let [out-chan (as/chan)
        _ (log/debug "Start Randomaction to state handler")]
    (as/go (loop [state 10]
             (let [[v ch] (as/alts! [quit-chan (as/timeout 100)])]
               (cond (= ch quit-chan)
                     (log/info "stopped action handler")
                     :else
                     (let [rand-action (rand-nth [:up :down :right :left])
                           next-state (state/get-new-position
                                       state rand-action maze)]
                       (log/info {:agent :ramdom-walk
                                  :action rand-action
                                  :state next-state})
                       (as/>! out-chan [state next-state])
                       (recur next-state))))))
    out-chan))

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
  [quit-chan maze]
  (let [out-chan (as/chan)
        _ (log/debug "Start Go Left to state handler")]
    (as/go (loop [state 20 action :up]
             (let [[v ch] (as/alts! [quit-chan (as/timeout 100)])]
               (cond (= ch quit-chan)
                     (log/info {:agent :go-left
                                :action :stop
                                :allert "stopped action handler"})
                     :else
                     (let [new-state (state/get-new-position
                                       state action maze)
                           next-action (get-next-action state new-state action)
                           ]
                       (log/info {:agent :go-left
                                  :action action
                                  :state new-state})
                       (as/>! out-chan [state new-state])
                       (recur new-state next-action))))))
    out-chan))

(defn fan-in [ins]
  (let [c (as/chan)]
    (as/go (while true
          (let [[x] (as/alts! ins)]
            (as/>! c x))))
    c))

(defn fan-out [in cs-or-n]
  (let [cs (if (number? cs-or-n)
             (repeatedly cs-or-n as/chan)
             cs-or-n)]
    (as/go (while true
          (let [x (as/<! in)
                outs (map #(vector % x) cs)]
            (as/alts! outs))))
    cs))


(defn in-out [in out]
  (as/go
   (while true
     (let [x (as/<! in)]
       (as/>! out x)))))

(defn split-keys
  "Given a chan of keyboard presses split in arrows an q"
  [in-keys-ch]
  (let [arrows #{"Up" "Down" "Left" "Right"}
        quit-key #{"q" "Q"}
        k1-out (as/chan)
        k2-out (as/chan)
        key-bc-ch-out (as-lab/broadcast k1-out k2-out)
        arrow-ch (keys/filter-key-map-chan k1-out arrows)
        quit-key-ch (keys/filter-key-map-chan k2-out quit-key)]
    (in-out in-keys-ch key-bc-ch-out)
    [arrow-ch quit-key-ch]))

(defn inner-main [columns rows]
  (let [
        [q1-out q2-out q3-out q4-out q5-out] [(as/chan) (as/chan) (as/chan) (as/chan) (as/chan)]
        quit-bc-ch-out (as-lab/broadcast q1-out q2-out q3-out q4-out q5-out)

        _ (log/debug (str "Generate Maze " columns "x" rows))
        maze (generate/generate-maze [columns rows])
        _ (log/debug "Show Swing GUI")
        labels (map swing-gui/make-maze-tile (:board maze))

        key-ch-in (swing-gui/setup-gui maze labels q1-out)
        [arrow-ch-in quit-key-ch-in] (split-keys key-ch-in)
        st1-ch-in (action-2-next-state arrow-ch-in q1-out maze)
        st2-ch-in (random-walk q2-out maze)
        st3-ch-in (keep-to-the-left q3-out maze)
        st-all-ch-in (fan-in [st1-ch-in st2-ch-in st3-ch-in])]
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
