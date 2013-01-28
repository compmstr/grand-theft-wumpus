(ns gtw.player
  (:require [gtw.city-map :as city-map]))

(declare go-to
         walk-to
         charge-to)

(def player
  "Player Data"
  (atom {:loc nil
         :inventory []
         :visited []}))

(defn start-player
  [locs]
  (go-to locs (rand-nth
               (map :id
                    (city-map/get-free-locs locs)))))

(defn go-to
  [locs loc]
  (swap! player
         #(-> %
              (assoc-in [:loc] loc)
              (update-in [:visited] (fn [_] (conj (:visited %) loc))))))