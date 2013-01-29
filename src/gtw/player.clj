(ns gtw.player
  (:require [gtw.city-map :as city-map]
            [gtw.ui :as ui]))

(defonce player
  (atom {:loc nil
         :inventory []
         :visited []}))

(defn move-player
  [loc]
  (swap! player
         #(-> %
              (assoc-in [:loc] loc)
              (update-in [:visited] (fn [_] (distinct (conj (:visited %) loc)))))))

(defn move-player-random
  "Moves player to a random free location"
  [locs]
  (move-player (:id (rand-nth (city-map/get-free-locs locs)))))
(defn start-player
  [locs]
  (move-player-random locs))

(defn handle-cops
  []
  (println "Caught by the cops!")
  nil)
(defn handle-worm
  [locs]
  (println "Found a worm gang")
  (move-player-random locs))
(defn handle-wumpus
  []
  (println "Found the wumpus!!!")
  (if (ui/charge?)
    true
    nil))

(defn go-to
  "Moves player to new location. Returns either the player map, or true for win, nil for lose"
  [locs loc]
  (let [new-loc (nth locs loc)]
    (cond
     (:worm new-loc)
     (handle-worm locs),
     (contains? (set (:cops new-loc)) (:loc @player))
     (handle-cops),
     (:wumpus new-loc)
     (handle-wumpus),
     true
     (move-player loc))))