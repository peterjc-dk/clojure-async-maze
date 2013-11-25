(ns maze.agents.left_test
  (:require [maze.state :as state]
            [maze.generate :as generate]
            [maze.util :as util]
            [maze.agents.left :as left]
            [clojure.core.async :as as]
            [midje.sweet :as m]
            [clojure.test :as test]))

(defn try-left
  "Start the go left agent and make asserts"
  []
  (let [maze (generate/generate-maze [(+ 1 (rand-int 20)) (+ 1 (rand-int 20))])
        sample-size (rand-int 100)
        board-size  (count (:board maze))
        start-state (rand-int board-size)
        pos-set (set (range board-size))
        qc (as/chan)
        rc (left/keep-to-the-left qc maze start-state 2)
        ls (util/chan-2-lazy-seq rc)]
    [(every? #(= :left-walk  %)
             (map first (take sample-size (ls))))
     (every? #(contains? pos-set %)
             (map second (take sample-size (ls))))
     (every? #(contains? pos-set %)
             (map last (take sample-size (ls))))]))

(test/deftest test-left-walker
  (dotimes [n 10]
    (m/fact (try-left) => (m/just [true? true? true?]))))
