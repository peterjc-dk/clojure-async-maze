(ns maze.agents.arrow_test
  (:require [maze.state :as state]
            [maze.generate :as generate]
            [maze.util :as util]
            [maze.agents.arrow :as arrow]
            [clojure.core.async :as as]
            [midje.sweet :as m]
            [clojure.test :as test]))

(defn try-arrow
  "Start the arrow agent and make asserts"
  []
  (let [maze (generate/generate-maze [(+ 1 (rand-int 20)) (+ 1 (rand-int 20))])
        sample-size 10
        board-size  (count (:board maze))
        pos-set (set (range board-size))
        start-state (rand-int board-size)
        sample-list (repeatedly (+ 10 sample-size) (fn [] (rand-nth [:left :up :down :right :noise])))
        qc (as/chan)
        in-chan (util/sq-2-chan sample-list)
        rc (arrow/arrow-to-state in-chan qc  maze start-state :user-walker 2)
        ls (util/ch-2-lazy-timeout rc 200)]
    [(every? #(= :user-walker %)
             (map first (take sample-size ls)))
     (every? #(contains? pos-set %)
             (map second (take sample-size ls)))
     (every? #(contains? pos-set %)
             (map last (take sample-size ls)))]))

(test/deftest test-arrow-walker
  (dotimes [n 10]
    (m/fact (try-arrow) => (m/just [true? true? true?]))))
