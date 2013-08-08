(ns maze.swing
  (:require [clojure.core.async :as as]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [seesaw.core :as saw]
            [seesaw.icon :as saw-icon]
            [maze.keys :as keys]
            [maze.state :as state]
            [logging.core :as log]))

(defn make-frame
  "create the swing grid frame given a list of labels"
  [title columns rows labels]
  (saw/frame :title title
          :size [(* columns 24) :by (* rows 24)]
          :on-close :dispose
          :content  (saw/grid-panel
                     :hgap 0 :vgap 0 :border 0
                     :columns columns
                     :rows  rows
                     :items labels )))

(defn neighbors-2-border
  "given a list of Neighbors create a border that visualize this"
  [neighbors]
  (let [neighbor-default-map {:up 2 :down 2
                              :left 2 :right 2}
        neighbor-map (reduce #(assoc %1 %2 0)
                             neighbor-default-map neighbors)]
    (seesaw.border/line-border :color :black
                               :top  (neighbor-map :up)
                               :bottom (neighbor-map :down)
                               :left (neighbor-map :left)
                               :right (neighbor-map :right))))

(defn make-maze-tile
  "Create a tile peace of the maze"
  [neighbors]
  (saw/label :text  ""
             :border (neighbors-2-border neighbors)
             :background :black
             :foreground :black))

(defn setup-gui
  "Setup the swing/seasaw gui"
  [maze labels quit-chan]
  (let [e-chan (as/chan)
        columns (:columns maze)
        rows (:rows maze)
        f (make-frame "The Maze" columns rows (into [] labels))
        key-handler (fn [e] (as/go
                            (as/>! e-chan
                                (keys/key-pressed-event-2-key-pressed-map e))))]
    (saw/listen f :key-pressed key-handler)
    (saw/show! f)
    (as/go
     (let [[v ch] (as/alts! [quit-chan])]
       (saw/hide! f)
       (saw/dispose! f)
       (log/debug "GUI Frame disposed")))
    e-chan))

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
                  label)))
  )

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

(defn change-label
  "Given a label change it"
  [label agent action]
  (change-label-config label (agent-action-lookup agent action)))

;((saw/dialog :content "hello"))
;(-> (dialog :content form) pack! show!)
;
(defn switch-state
  "Given old and new state update labels"
  [labels agent old-state new-state]
  (let [
        leave-label (nth labels old-state)
        enter-label (nth labels new-state)]
    (change-label leave-label agent :leave)
    (change-label enter-label agent :enter)))

(defn init-tile
  "init the given label"
  [labels tile-index]
  (let [label (nth labels tile-index)
        bg-color (seesaw.color/get-rgba (saw/config label :background))
        black-rgba [0 0 0 255]]
    (when (= bg-color black-rgba)
      (change-label label :arrow-day-walker :init))))

(defn draw-maze
  "Given the labels and a chan with events update gui"
  [labels maze quit-chan timeout goal-index]
  (let [_ (log/debug "Draw Maze START")
        states (-> labels count
                   dec range set)
        [c r] [(:columns maze) (:rows maze)]
        start-state (state/position-to-index (first (:path maze)) [c r])
        _ (init-tile labels start-state)]
    (as/go
     (loop [state start-state path (rest (:path maze))]
       (let [[v ch] (as/alts! [quit-chan (as/timeout timeout)])]
         (cond (or (empty? path) (= ch quit-chan))
               (do
                 (log/info {:agent :draw-maze
                            :action :stop
                            :allert "Drawing maze stopped"}))
               :else
               (let [new-state (state/position-to-index (first path) [c r])]
                 (log/info {:agent :draw-maze
                            :action [state new-state]})
                 (when (contains? states new-state)
                   (init-tile labels new-state))
                 (recur new-state (rest path)))))))))

(defn are-we-there-yet?
  "Check if the new state is the goal state"
  [state goal]
  (if (= state goal)
    (-> (saw/dialog :content "We are here, but are we there yet?"
                    :option-type :yes-no)
        saw/pack!
        saw/show!)
    false))

(defn welcome-form
  [default-columns default-rows]
  (saw/grid-panel
   :columns 2
   :items ["Welcome "  ""
           "Columns"      (saw/spinner :id :columns
                                       :tip "Number of columns in the Maze (must be positive)"
                                       :model (saw/spinner-model default-columns :from 1 :to 200))
           "Rows"         (saw/spinner :id :rows
                                       :tip "Number of rows in the Maze (must be positive)"
                                       :model (saw/spinner-model default-rows :from 1 :to 200))
           "Day or Night" (saw/combobox :id :day-or-night
                                        :model ["Day" "Night"])]))

(defn welcome-pop
  "Pop up a a welcome frame and ask you some things"
  [default-columns default-rows]

  (let [form (welcome-form default-columns default-rows)]
    (-> (saw/dialog :content form)
       saw/pack!
       saw/show!)
    (update-in (saw/value form)
               [:day-or-night]
               #(-> %
                    str
                    str/lower-case
                    keyword))))


(defn change-gui
  "Given the labels and a chan with events update gui"
  [labels in-chan quit-chan goal-index]
  (let [_ (log/debug "GUI handler START")
        states (set (range (count labels)))
        ;_ (switch-state labels 0 0) ;; TODO fix this hard coding
        _ (change-label (nth labels goal-index) :arrow-day-walker :goal) ;; TODO goal is TDB
        ]
    (as/go
     (loop []
       (let [[v ch] (as/alts! [quit-chan in-chan])]
         (cond (= ch quit-chan)
               (do (log/info {:agent :change-gui
                              :action :stop
                              :allert "Stop change GUI go-loop"})
                   :stop)
               :else
               (let [[agent old-state new-state] v]
                 (log/info {:agent :change-gui
                            :action [agent old-state new-state]})
                 (when (contains? states new-state)
                   (switch-state labels agent old-state new-state)
                   (let [we-are-there (are-we-there-yet? new-state goal-index)]
                     (if-not we-are-there
                      (recur)
                      we-are-there))))))))))

;:param-string (.paramString event)
