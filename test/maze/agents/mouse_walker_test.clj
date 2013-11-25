(ns maze.agents.mouse_walker_test
  (:require [maze.state :as state]
            [maze.generate :as generate]
            [maze.util :as util]
            [maze.agents.mouse_walker :as mouse]
            [clojure.core.async :as as]
            [midje.sweet :as m]
            [clojure.test :as test]))

(defn try-mouse
  "Start the arrow agent and make asserts"
  []
  (let [maze (generate/generate-maze [(+ 1 (rand-int 20)) (+ 1 (rand-int 20))])
        sample-size 10
        board-size  (count (:board maze))
        number-of-clicks  (* sample-size board-size)
        pos-set (set (range board-size))
        start-state (rand-int board-size)
        sample-list (map #(assoc {:event :mouse-clicked} :tile %)
                         (repeatedly (+ 10 number-of-clicks) (fn [] (rand-int board-size))))
        qc (as/chan)
        in-chan (util/sq-2-chan sample-list)
        rc (mouse/mouse-to-state in-chan qc maze start-state :night)
        ls (util/ch-2-lazy-timeout rc 200)]
    [(every? #(= :arrow-night-walker %)
             (map first (take sample-size ls)))
     (every? #(contains? pos-set %)
             (map second (take sample-size ls)))
     (every? #(contains? pos-set %)
             (map last (take sample-size ls)))]))

(test/deftest test-mouse-walker
  (dotimes [n 10]
    (m/fact (try-mouse) => (m/just [true? true? true?]))))
