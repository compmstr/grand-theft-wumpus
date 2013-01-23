(ns grand-theft-wumpus.player)

(def player
  "Player Data"
  (atom {:loc nil
         :inventory []
         :visited []}))