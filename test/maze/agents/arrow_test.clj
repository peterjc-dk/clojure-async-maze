(ns maze.agents.arrow_test
  (:require [maze.state :as state]
            [maze.generate :as generate]
            [maze.util :as util]
            [maze.agents.arrow :as arrow]
            [clojure.core.async :as as]
            [midje.sweet :as m]
            [clojure.test :as test]
            [criterium.core :as criterium]))

(defn try-arrow
  "Start the arrow agent and make asserts"
  []
  (let [_ (println "Try Arrow")
        maze (generate/generate-maze [(+ 1 (rand-int 50)) (+ 1 (rand-int 50))])
        sample-size 1
        board-size  (count (:board maze))
        pos-set (set (range board-size))
        start-state (rand-int board-size)
        qc (as/chan)
        _ (println "hello")
        in-chan (util/sq-2-chan [:left :right])
        rc (arrow/arrow-to-state in-chan qc maze start-state)
        ls (util/chan-2-lazy-seq rc)]
    (and (every? #(contains? pos-set %)
                 (map first (take sample-size (ls))))
         (every? #(contains? pos-set %)
        (map second (take sample-size (ls)))))))

(println (try-arrow))
(test/deftest test-arrow-walker
  (dotimes [n 10]
    (m/fact (try-arrow) => true)))
