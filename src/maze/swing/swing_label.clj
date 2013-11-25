(ns maze.swing.swing_label
  (:require [clojure.core.async :as as]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [seesaw.core :as saw]
            [seesaw.icon :as saw-icon]
            [maze.keys :as keys]
            [maze.state :as state]
            [maze.swing.swing_agent_map :as agent-map]))

(defn scale-icon
  "Given a icon and w,h return a scaled icon "
  [icon w h]
  (let [img (.getImage icon)
        smooth java.awt.Image/SCALE_SMOOTH
        scaled-img (.getScaledInstance img w h smooth)]
    (saw-icon/icon scaled-img)))

(def scale-icon-memoize (memoize scale-icon))

(defn scale-icon-to-label
  "given a label and a icon,
return a icon scaled to the size of the label"
  [icon label]
  (let [size (saw/config label :size)
        [w h] [(max 10 (.width size))
               (max 10 (.height size))]]
    (scale-icon-memoize icon w h)))

(defn change-border-color
  "Given a border return a copy with the color changed"
  [border color]
  (let [in-sets (.getBorderInsets border)
        col (seesaw.color/color color)]
    (javax.swing.border.MatteBorder. in-sets col)))

(defn change-label-icon
  "Given a label atribute and new-value change it if new-value not nil"
  [label icon]
  (if icon
    (saw/config!
     label :icon (scale-icon-to-label
                  icon
                  label))
    (saw/config!
     label :icon nil)))

(defn change-label-background
  "Given a label atribute and new-value change it if new-value not nil"
  [label background-color]
  (when background-color
    (saw/config!
     label :background background-color)))

(defn change-label-border
  "Given a label atribute and new-value change it if new-value not nil"
  [label border-color]
  (when border-color
    (let [border (saw/config label :border)]
      (saw/config! label :border
                   (change-border-color border border-color)))))

(defn change-label-config
  "do the config! on a given label"
  [label icon border-color background-color]
  (do
    (change-label-icon label icon)
    (change-label-border label border-color)
    (change-label-background label background-color)))

(defn change-label
  "Given a label change it"
  [label agent action border-color]
  (let [agent-map (agent-map/agent-action-lookup agent action)
        icon (:icon agent-map)
        background-color (:background-color agent-map)]
    (change-label-config label icon border-color background-color)))

(defn set-goal
  "do the config! on a label give a index"
  [labels goal-index]
  (let [label (nth labels goal-index)
        goal-icon (saw-icon/icon
                  (io/resource "clojure-logo.png"))]
    (change-label-config label goal-icon :black :white)))

(defn switch-state
  "Given old and new state update labels"
  [labels agent old-state new-state day-or-night]
  (let [leave-label (nth labels old-state)
        enter-label (nth labels new-state)
        day-night-border-color (if (= day-or-night :night) :red :black)]
    (change-label leave-label agent :leave day-night-border-color)
    (change-label enter-label agent :enter day-night-border-color)
    (when (= agent :left-walk)
      (let [leave-txt (saw/config leave-label :text)
            enter-txt (saw/config enter-label :text)
            leave-val (if (not= leave-txt "") (read-string leave-txt) 0)
            enter-val (if (not= enter-txt "")
                        (min (read-string enter-txt)
                             (inc leave-val))
                          (inc leave-val))]
        (saw/config! leave-label :text (str leave-val))
        (saw/config! enter-label :text (str enter-val))))))
