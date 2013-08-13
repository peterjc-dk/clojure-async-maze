(ns maze.generate_test
  (:require [maze.generate :as gen]
            [midje.sweet :as m]))

(m/fact (gen/insert-node-to-visited #{[0 0]} [1 1]) => #{[0 0] [1 1]})
(m/fact (gen/insert-node-to-visited #{[0 0] [1 1]} [1 1]) => #{[0 0] [1 1]})

(m/fact (gen/insert-edge-to-graph [[] []] [2 1] {:action :left
                                       :from [0 0]
                                       :to [1 0]}) => [[:left] [:right]])
(m/fact (gen/insert-to-frontier
         [{:action :right :from [1 0] :to [0 0]}]
         [{:action :left :from [0 0] :to [1 0]}]) =>
         [{:action :right :from [1 0] :to [0 0]}
          {:action :left :from [0 0] :to [1 0]}])

(m/fact (gen/insert-to-frontier [] nil) => [])

(m/fact (gen/get-not-visited-neighbourg-edges [1 1] [3 3] [[1 0]]) =>
        [{:action :down, :from [1 1], :to [1 2]}
         {:action :left, :from [1 1], :to [0 1]}
         {:action :right, :from [1 1], :to [2 1]}])


(m/facts
 (m/fact (count (remove #(empty? %)
                        (:board (gen/generate-maze [2 2])))) => 4)
 (m/fact (count (remove #(empty? %)
                        (:board (gen/generate-maze [5 5])))) => 25)
 (m/fact (count (remove #(empty? %)
                       (:board (gen/generate-maze [10 15])))) => 150))
