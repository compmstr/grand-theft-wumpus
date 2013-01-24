(ns grand-theft-wumpus.city-map
  (require [grand-theft-wumpus.config :as config]))

(defn rand-set
  [num max]
  (if (< max num)
    (throw (IllegalArgumentException. "Can't create a set with more items than available numbers")))
  (loop [rset (set (take num (repeatedly #(rand-int max))))]
    (if (= (count rset) num)
      (shuffle rset)
      (recur (set (concat
                   rset
                   (take (- num (count rset)) (repeatedly #(rand-int max)))))))))

(defn random-loc
  []
  (rand-int config/city-locs))

(defn add-worms
  [locs]
  (loop [worm-locs (rand-set config/num-worms config/city-locs)
         locs locs]
    (if (empty? worm-locs)
      locs
      (recur (rest worm-locs)
             (update-in locs
                        [(first worm-locs) :worm]
                        (fn [_] true))))))

(defn get-city-roads
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

(defn add-cop-roadblock
  "Adds a cop roadblock on the connection between two indexed locs"
  [locs pair]
  (update-in
   (update-in locs [(first pair) :cops]
              conj (second pair))
   [(second pair) :cops]
   conj (first pair)))

(defn add-cops
  [locs]
  (let [roads (apply vector (get-city-roads locs))
        num-roads (count roads)]
    (reduce (fn [locs conn]
              (if (zero? (rand-int config/cop-odds))
                (add-cop-roadblock locs conn)
                locs))
            locs roads)))

(defn add-road-pair
  [locs pair]
  (update-in
   (update-in locs [(first pair) :connections]
              conj (second pair))
   [(second pair) :connections]
   conj (first pair)))
(defn add-roads
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

(defn get-connected
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
            
(defn find-islands
  [locs]
  (loop [to-check (range config/city-locs)
         islands []]
    (if (empty? to-check)
      islands
      (let [new-island (get-connected locs (first to-check))]
        (recur
         (remove #(contains? new-island %) to-check)
         (conj islands new-island))))))

(defn connect-islands
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

(defn base-city-loc
  [id]
  {:id id
   :connections []
   :worm nil
   :cops []})
(defn base-city
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
      ))

(def city-map
  "Map of congestion city, a vector of city nodes"
  (atom []))