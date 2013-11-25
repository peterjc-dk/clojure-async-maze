(ns maze.swing.swing_pop
  (:require [clojure.string :as str]
            [seesaw.core :as saw]))

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
