(ns maze.generate
  (:require [maze.state :as state]))

(defn insert-node-to-visited
  "Given set of visited nodes return a new set with a elment added"
  [visited node]
  (conj visited node))

(defn add-action-to-graph
  "Given a graph, node and action return a graph with the action added to the node "
  [graph [columns rows] node action]
  (let [node-index (state/position-to-index
                    node [columns rows])]
    (assoc graph node-index
           (conj (get graph node-index) action))))

(defn insert-edge-to-graph
  "Given a graph and a edge return a new graph with the edge added"
  [graph [columns rows] edge]
  (let [from (edge :from)
        to (edge :to)
        action (edge :action)
        rev-action (state/get-reverse-action action)]
    (-> (add-action-to-graph graph [columns rows] from action)
        (add-action-to-graph [columns rows] to rev-action))))

(defn insert-to-frontier
  "Given the graph frontier insert edges "
  [frontier edges]
  (if (and edges (first edges))
    (distinct (apply conj frontier edges))
    ;; else
    frontier))

(defn get-not-visited-neighbourg-edges
  "Given a position its and visited states return un visited neighbourgs"
  [[i j] [columns rows] visited]
  (->> (state/neighbourgs [i j] [columns rows])
       (filter #(not (contains? (set visited) (second %))))
       (map (fn [x] {:action (first x) :from [i j] :to (second x)}))
       (into [])))

(defn filter-frontier
  "Given the frontier filter out irrelevant edges"
  [frontier edge]
  (->> (remove #(= edge %) frontier)
       (remove #(= (:to edge) (:to %)))))

(defn generate-empty-board
  "given size [columns rows] a empty board is generated"
  [[columns rows]]
  (into [] (take (* columns rows) (iterate identity []))))

(defn generate-maze
  "given size [columns rows] a maze is generated"
  [[columns rows]]
  (let [empty-board (generate-empty-board [columns rows])
        start-column (rand-int (- columns 1))
        start-row (rand-int rows)
        start-frontier [{:action :right
                        :from [start-column start-row]
                        :to [(+ 1 start-column) start-row]}]]
    (loop [visited #{[start-column start-row]}
           frontier start-frontier
           board empty-board
           i 0
           vistit-path [[start-column start-row]]]
      (if (or (empty? frontier) (= (count visited) (count board)))
        {:columns columns
         :rows rows
         :board board
         :path vistit-path}
        ;; else
        (let [next-edge (rand-nth frontier)
              shorter-frontier (filter-frontier frontier next-edge)
              node (:to next-edge)
              next-visited (insert-node-to-visited visited node)
              not-visited-neighbourg-edges (get-not-visited-neighbourg-edges
                                            node [columns rows] next-visited)
              next-frontier (insert-to-frontier shorter-frontier
                                                not-visited-neighbourg-edges)
              next-board (insert-edge-to-graph board [columns rows] next-edge)]
          (recur next-visited
                 next-frontier
                 next-board
                 (inc i)
                 (conj vistit-path node)))))))

(defn print-maze
  "Given a maze pprint it"
  [maze]
  (doseq [line (partition (:columns maze) (:board maze))]
    (println line)))
