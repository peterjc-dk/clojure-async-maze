(ns maze.state)

(defn index-to-position
	"index to position [i j]"
	[index [columns rows]]
	(let [size (* columns rows)
              mod-i (mod index size)]
          [(rem mod-i columns) (int (/ mod-i columns))]))

(defn position-to-index
	"position [i j] to a index"
	[[i j] [columns rows]]
        (mod (+ i (* columns j)) (* columns rows)))

(defn valid-position?
  "is the i j a valid position"
  [[i j] [columns rows]]
  (let [valid-col-index (set (range 0 columns))
        valid-row-index (set (range 0 rows))]
    (and (contains? valid-col-index i)
         (contains? valid-row-index j))))

(defn get-tile-on-board
  "get the tile element at position [i j]"
  [[i j] [columns rows] board]
  (if (and (valid-position? [i j] [columns rows])
           (= (count board) (* columns rows)))
    (nth board (position-to-index [i j] [columns rows]))))

(defn neighbourgs
  "return all neighbourg positions to position i j "
  [[i j] [columns rows]]
  (let [all-neighbourgs {:up [i (dec j)]
                         :down [i (inc j)]
                         :left [(dec i) j]
                         :right [(inc i) j]}]
    (into {} (filter
              #(valid-position? (second %) [columns rows]) all-neighbourgs))))

(defn get-reverse-action
  "given a action :up :down :left right return the reverse"
  [action]
  (action {:up :down
           :down :up
           :left :right
           :right :left}))

(defn get-new-position
  "Given position index, a action and a maze if action possible return new position index, else return pressent position index (dont move)"
  [index action {columns :columns rows :rows board :board}]
  (let [[i j] (index-to-position index [columns rows])]
    (if (valid-position? [i j] [columns rows])
     (let [position [i j]
           tile-actions (set (get-tile-on-board
                              position [columns rows] board))
           nb (neighbourgs position [columns rows])
           new-position (get nb action)
           rev-act (get-reverse-action action)]
       (if (and (contains? tile-actions action)
                new-position rev-act
                (contains? (set (get-tile-on-board
                                 new-position
                                 [columns rows] board)) rev-act))
         (position-to-index new-position [columns rows])
         ;; else
         index)))))
