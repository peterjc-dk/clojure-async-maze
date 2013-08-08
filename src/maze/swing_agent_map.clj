(ns maze.swing_agent_map
  (:require [clojure.java.io :as io]
            [seesaw.core :as saw]
            [seesaw.icon :as saw-icon]))

(def agent-map
  {:arrow-day-walker
   {:enter {:icon (saw-icon/icon
                   (io/resource "you-are-here-dot.png"))
            :border-color :black
            :background-color :white}
    :leave {:icon (saw-icon/icon
                   (io/resource "dot.png"))
            :border-color :black
            :background-color :white}
    :goal {:icon (saw-icon/icon
                  (io/resource "clojure-logo.png"))
           :border-color :black
           :background-color :white}
    :init {:icon nil
           :border-color :black
           :background-color :white}}

   :arrow-night-walker
   {:enter {:icon (saw-icon/icon
                   (io/resource "you-are-here-dot.png"))
            :border-color :red
            :background-color :white}
    :leave {:icon (saw-icon/icon
                   (io/resource "dot.png"))
            :border-color :red
            :background-color :white}
    :goal {:icon (saw-icon/icon
                  (io/resource "clojure-logo.png"))
           :border-color :black
           :background-color :white}
    :init {:icon nil
           :border-color :black
           :background-color :white}}

   :ramdom-walk
   {:enter {:icon (saw-icon/icon
                   (io/resource "you-are-here-dot.png"))
            :border-color :black
            :background-color :white}
    :leave {:icon (saw-icon/icon
                   (io/resource "dot.png"))
            :border-color :black
            :background-color :white}
    :goal {:icon (saw-icon/icon
                  (io/resource "clojure-logo.png"))
           :border-color :black
           :background-color :white}
    :init {:icon nil
           :border-color :black
           :background-color :white}}

   :left-walk
   {:enter {:icon (saw-icon/icon
                   (io/resource "you-are-here-dot.png"))
            :border-color :black
            :background-color :white}
    :leave {:icon (saw-icon/icon
                   (io/resource "dot.png"))
            :border-color :black
            :background-color :white}
    :goal {:icon (saw-icon/icon
                  (io/resource "clojure-logo.png"))
           :border-color :black
           :background-color :white}
    :init {:icon nil
           :border-color :black
           :background-color :white}}})

(defn agent-action-lookup
  "well"
  [agent action]
  (get-in agent-map [agent action]))
