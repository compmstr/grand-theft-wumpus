(ns gtw.player
  (:require [gtw.city-map :as city-map]
            [gtw.ui :as ui]))

(defonce player
  (atom {}))

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
  (reset! player {:loc nil
                  :inventory []
                  :visited []})
  (move-player-random locs))

(defn handle-cops
  []
  (ui/set-message "Caught by the cops!")
  nil)
(defn handle-worm
  [locs]
  (ui/set-message "Found a worm gang")
  (move-player-random locs))
(defn handle-wumpus
  []
  (ui/set-message "Found the wumpus!!!")
  (if (ui/charge?)
    (do
      (ui/set-message
       "You Killed your ex-partner, the wumpus!")
      true)
    (do
      (ui/set-message
       "You run into the wumpus, and he pumps you full of lead...")
      nil)))

(defn go-to
  "Moves player to new location. Returns either the player map, or true for win, nil for lose"
  [locs loc]
  (let [new-loc (city-map/id->loc locs loc)]
    (cond
     (:worm new-loc)
     (handle-worm locs),
     (contains? (set (:cops new-loc)) (:loc @player))
     (handle-cops),
     (:wumpus new-loc)
     (handle-wumpus),
     true
     (move-player loc))))