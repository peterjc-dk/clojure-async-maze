(ns maze.state_test
  (:require [maze.state :as state]
            [clojure.test :as test]
            [midje.sweet :as m]))

(test/deftest state-change-functions

  (m/facts
   (m/fact (state/index-to-position -1 [2 3]) => [1 2])
   (m/fact (state/index-to-position 0 [2 3]) => [0 0])
   (m/fact (state/index-to-position 1 [2 3]) => [1 0])
   (m/fact (state/index-to-position 2 [2 3]) => [0 1])
   (m/fact (state/index-to-position 3 [2 3]) => [1 1])
   (m/fact (state/index-to-position 4 [2 3]) => [0 2])
   (m/fact (state/index-to-position 5 [2 3]) => [1 2])
   (m/fact (state/index-to-position -1 [2 3]) => (state/index-to-position 5 [2 3])))


  (m/facts
   (m/fact (state/position-to-index [0 0] [2 3]) => 0)
   (m/fact (state/position-to-index [-1 0] [2 3]) => 5)
   (m/fact (state/position-to-index [1 2] [2 3]) => 5))

  (m/fact (state/valid-position? [-1 0] [2 2]) => false)
  (m/fact (state/valid-position? [0 0] [2 2]) => true)
  (m/fact (state/valid-position? [1 1] [2 2]) => true)
  (m/fact (state/valid-position? [1 2] [2 2]) => false)
  (m/fact (state/valid-position? [2 1] [3 2]) => true)
  (m/fact (state/valid-position? [3 1] [3 2]) => false)

  (m/fact (state/get-tile-on-board [0 0] [1 1] [[:up]]) => [:up])
  (m/fact (state/get-tile-on-board [0 0] [1 1] []) => nil)
  (m/fact (state/get-tile-on-board [-1 0] [1 1] []) => nil)

  (m/fact (state/neighbourgs [0 0] [2 2]) => {:down [0 1] :right [1 0]})
  (m/fact (state/neighbourgs [1 1] [3 3]) => {:down [1 2] :right [2 1]
                                              :up [1 0] :left [0 1]})

  (m/fact (state/get-reverse-action :left) => :right)
  (m/fact (state/get-reverse-action :hest) => nil)

  (m/fact (state/get-new-position 1 :down {:columns 2 :rows 2 :board [[:down] [] [] []]}) => 1)
  (m/fact (state/get-new-position 0 :down {:columns 2 :rows 2 :board [[:down] [] [:up] []]}) => 2))
