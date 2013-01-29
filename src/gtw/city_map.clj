(ns gtw.city-map
  (:require [gtw.config :as config])
  (:use gtw.util))

(declare get-within-two
         generate-map
         id->loc
         ids->locs
         get-free-locs)

(defn id->loc
  [locs id]
  (nth locs id))
(defn ids->locs
"Pass in a seq of ids, and a map, and this will return the
maps of data for those ids"
  [locs ids]
  (map (partial id->loc locs) ids))

(defn- random-loc
  []
  (rand-int config/city-locs))

(defn- add-worms
  [locs]
  (loop [worm-locs (rand-set config/num-worms config/city-locs)
         locs locs]
    (if (empty? worm-locs)
      locs
      (recur (rest worm-locs)
             (assoc-in locs
                        [(first worm-locs) :worm]
                        true)))))

(defn- get-city-roads
  "Returns a set of pair sets of connected locs"
  [locs]
  (set
   (flatten
    (map (fn [loc]
           (map
            (fn [conn]
              (set [loc conn]))
            (:connections (nth locs loc))))
         (range config/city-locs)))))

(defn- add-cop-roadblock
  "Adds a cop roadblock on the connection between two indexed locs"
  [locs pair]
  (update-in
   (update-in locs [(first pair) :cops]
              conj (second pair))
   [(second pair) :cops]
   conj (first pair)))

(defn- add-cops
  [locs]
  (let [roads (apply vector (get-city-roads locs))
        num-roads (count roads)]
    (reduce (fn [locs conn]
              (if (zero? (rand-int config/cop-odds))
                (add-cop-roadblock locs conn)
                locs))
            locs roads)))

(defn- add-wumpus
  "Adds the wumpus to an unoccupied location"
  [locs]
  (let [free-locs (get-free-locs locs)
        wumpus-loc (rand-nth free-locs)]
    (assoc-in locs [(:id wumpus-loc) :wumpus]
               true)))

(defn- add-road-pair
  [locs pair]
  (update-in
   (update-in locs [(first pair) :connections]
              conj (second pair))
   [(second pair) :connections]
   conj (first pair)))
(defn- add-roads
  [locs]
  (loop [loc-pairs (->> (take config/city-roads (repeatedly #(rand-set 2 config/city-locs)))
                       (map set)
                       set
                       (apply vector))
         locs locs]
    (if (empty? loc-pairs)
      locs
      (recur (rest loc-pairs)
             (add-road-pair locs (first loc-pairs))))))

(defn- get-connected
  "Takes in all locs, and the index to start at"
  [locs starting]
  (loop [connections (set (conj (:connections (nth locs starting))
                                starting))
         visited [starting]
         to-visit (:connections (nth locs starting))]
    (if (empty? to-visit)
      connections
      (let [new-conns (:connections (nth locs (first to-visit)))]
        (recur (set (concat new-conns connections))
               (conj visited (first to-visit))
               (concat (rest to-visit)
                       (filter #(not (contains? connections %)) new-conns)))))))
(defn- find-islands
  [locs]
  (loop [to-check (range config/city-locs)
         islands []]
    (if (empty? to-check)
      islands
      (let [new-island (get-connected locs (first to-check))]
        (recur
         (remove #(contains? new-island %) to-check)
         (conj islands new-island))))))

(defn- connect-islands
  "Connect any disjoined sets of locations in the city"
  [locs]
  (let [islands (find-islands locs)
        first-island (apply vector (first islands))]
    (loop [islands (rest islands)
           locs locs]
      (if (empty? islands)
        locs
        (recur
         (rest islands)
         (add-road-pair
          locs
          [(rand-nth first-island)
           (rand-nth (apply vector (first islands)))]))))))

(defn- base-city-loc
  "Generates a base city loc"
  [id]
  {:id id
   :connections []
   :cops []})
(defn- base-city
  []
  (apply vector
         (map base-city-loc (range config/city-locs))))

(defn generate-map
  []
  (-> (base-city)
      (add-worms)
      (add-roads)
      (connect-islands)
      (add-cops)
      (add-wumpus)
      ))

(defn- worm-close?
  [locs loc]
  (let [loc (if (map? loc) (:id loc) loc)]
    (not
     (empty?
      (filter :worm
              (map #(nth locs %)
                   (:connections loc)))))))

(defn get-within-two
  "Returns set of locs within two hops of starting"
  [locs starting]
  (set
   (apply concat
          (map #(:connections (nth locs %))
               (set (conj (:connections (nth locs starting)) starting))))))

(defn wumpus-close?
  [locs loc]
  (let [loc (if (map? loc) (:id loc) loc)]
    (not (empty?
          (filter :wumpus
                  (map #(nth locs %)
                       (get-within-two locs loc)))))))

(defn get-free-locs
  "Returns a list of locations with no worms, wumpuses, or cops"
  [locs]
  (->> locs
       (remove (partial worm-close? locs))
       (remove (partial wumpus-close? locs))
       (remove :worm)
       (remove :wumpus)
       (filter #(empty? (:cops %)))))

(defn- cop-labelled-connections
  "Returns the connections for a loc with the cop connections labeled as such,
for use with map-to-graph"
  [loc]
  (let [cop-set (set (:cops loc))]
    (map #(if (contains? cop-set %)
            {:label %
             :attrs {:label "Cops"}}
            %)
         (:connections loc))))

(defn map->graph
  [locs]
  (for [loc locs]
    {:label (:id loc)
     :attrs {:label (str (:id loc)
                         (when (wumpus-close? locs loc)
                           "\\nBlood!")
                         (when (:worm loc)
                           "\\nWorm!")
                         (when (:wumpus loc)
                           "\\nWumpus!")
                         (when (worm-close? locs loc)
                           "\\nGlow")
                         (when-not (empty? (:cops loc))
                           "\\nSirens"))}
     :connections (cop-labelled-connections loc)}))

(defn visited->graph
  "Generates a graph for the currently visible locations
    Expects a map with :visited and :loc keys"
  [locs {visited :visited cur :loc}]
  (for [loc-id visited :let [loc (nth locs loc-id)]]
    {:label loc-id
     :attrs {:label (str loc-id
                         (when (= cur loc-id)
                           "\\n*You*")
                         (when (wumpus-close? locs loc)
                           "\\nBlood!")
                         (when (:worm loc)
                           "\\nWorm!")
                         (when (:wumpus loc)
                           "\\nWumpus!")
                         (when (worm-close? locs loc)
                           "\\nGlow")
                         (when-not (empty? (:cops loc))
                           "\\nSirens"))}
     :connections (:connections loc)}))