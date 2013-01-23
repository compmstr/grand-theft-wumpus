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
(defn add-roads
  [locs]
  (let [loc-pairs (->> (take config/city-roads (repeatedly #(rand-set 2 config/city-locs)))
                       (map set)
                       set
                       (apply vector))]

(defn connect-islands
  [locs])

(defn base-city-loc
  []
  {:connections []
   :worm nil})

(defn generate-map
  []
  (-> (apply vector
             (take config/city-locs (repeatedly base-city-loc)))
      (add-worms)
      (add-roads)
      (connect-islands)
      ))

(def city-map
  "Map of congestion city, a vector of city nodes"
  (atom []))