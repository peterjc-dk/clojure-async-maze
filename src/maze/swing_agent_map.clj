(ns maze.swing_agent_map
  (:require [clojure.java.io :as io]
            [seesaw.core :as saw]
            [seesaw.icon :as saw-icon]))

(def agent-map
  {:user-walker
   {:enter {:icon (saw-icon/icon
                   (io/resource "you-are-here-dot.png"))
            :background-color :white}
    :leave {:icon (saw-icon/icon
                   (io/resource "dot.png"))
            :background-color :white}}

   :ramdom-walk
   {:enter {:icon (saw-icon/icon
                   (io/resource "you-are-here-dot.png"))
            :background-color :white}
    :leave {:icon (saw-icon/icon
                   (io/resource "dot.png"))
            :background-color :white}}

   :left-walk
   {:enter {:icon (saw-icon/icon
                   (io/resource "you-are-here-dot.png"))
            :background-color :white}
    :leave {:icon nil
            :background-color :white}}})

(defn agent-action-lookup
  "well"
  [agent action]
  (get-in agent-map [agent action]))
