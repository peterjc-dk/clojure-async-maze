(ns maze.swing_label
  (:require [clojure.core.async :as as]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [seesaw.core :as saw]
            [seesaw.icon :as saw-icon]
            [maze.keys :as keys]
            [maze.state :as state]
            [maze.swing_agent_map :as agent-map]
            [logging.core :as log]))

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
  (when icon
    (saw/config!
     label :icon (scale-icon-to-label
                  icon
                  label))))

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
  [label {icon :icon
          border-color :border-color
          background-color :background-color}]
  (do
    (change-label-icon label icon)
    (change-label-border label border-color)
    (change-label-background label background-color)))

(defn change-label
  "Given a label change it"
  [label agent action]
  (change-label-config label (agent-map/agent-action-lookup agent action)))

(defn switch-state
  "Given old and new state update labels"
  [labels agent old-state new-state]
  (let [
        leave-label (nth labels old-state)
        enter-label (nth labels new-state)]
    (change-label leave-label agent :leave)
    (change-label enter-label agent :enter)))
