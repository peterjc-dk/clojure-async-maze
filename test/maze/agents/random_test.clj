(ns maze.agents.random_test
  (:require [maze.state :as state]
            [maze.generate :as generate]
            [maze.util :as util]
            [maze.agents.random :as random]
            [clojure.core.async :as as]
            [midje.sweet :as m]
            [clojure.test :as test]
            [criterium.core :as criterium]))

(defn try-random
  "Start the random agent and make asserts"
  []
  (let [maze (generate/generate-maze [(+ 1 (rand-int 50)) (+ 1 (rand-int 50))])
        sample-size (rand-int 1000)
        board-size  (count (:board maze))
        start-state (rand-int board-size)
        pos-set (set (range board-size))
        qc (as/chan)
        rc (random/random-walk qc maze start-state 2)
        ls (util/chan-2-lazy-seq rc)]
    (and (every? #(contains? pos-set %)
                 (map first (take sample-size (ls))))
         (every? #(contains? pos-set %)
        (map second (take sample-size (ls)))))))

(test/deftest test-random-walker
  (dotimes [n 10]
    (m/fact (try-random) => true)))


(comment
  (criterium/quick-bench
   (try-random)))
