(ns gtw.core
  (:require [gtw.city-map :as city-map]
            [gtw.player :as player]
            [gtw.ui :as ui]
            gviz))

(defonce map-file (.getAbsolutePath (java.io.File/createTempFile "gtw.game-map" ".png")))

(defonce game-map (city-map/generate-map))
(defn game-dot-string
  []
  (gviz/graph->dot
   (city-map/visited->graph game-map @player/player)
   :concentrate "true"
   :size "4,3"
   :dpi "200"))

(declare game-move)
(defn game-display
  [new-loc]
  (gviz/render-dot-string (game-dot-string) map-file)
  (ui/set-img map-file)
  (let [player-visited (set (:visited @player/player))]
    (ui/set-buttons (apply hash-map
                           (apply concat
                                  (for [conn
                                        (:connections
                                         (city-map/id->loc game-map new-loc))]
                                    [(if (contains? player-visited conn)
                                       (str "[" conn "]")
                                       conn)
                                     #(game-move conn)]))))))

(defn game-end
  [message]
  (ui/pop-up-message message)
  (ui/reset-ui)
  (def game-map (city-map/generate-map))
  (player/start-player game-map)
  (game-display (:loc @player/player)))

(defn game-move
  [new-loc]
  (let [move-result (player/go-to game-map new-loc)]
    (if (map? move-result)
      (game-display (:loc @player/player))
      (if move-result
        (game-end "You beat the wumpus!, you won!")
        (game-end "Too Bad, try again")))))

(defn -main
  []
  (player/start-player game-map)
  (ui/start-ui)
  (game-display (:loc @player/player))
  (.deleteOnExit (java.io.File. map-file)))
;;(print (game-dot-string))
;;(ui/start-ui)
;;(gviz/render-dot-string (game-dot-string) map-file)
;;Delete the map file when we're done with the game

;;(ui/set-img map-file)

;;(player/go-to game-map <new-loc>)

;;(gviz/render-dot-string (game-dot-string) map-file)
;; then update the image on the UI

;;Example of displaying map
;;(spit "map.dot"
      ;;(-> (city-map/generate-map)
          ;;(city-map/map->graph)
          ;;(gviz/graph->dot :concentrate "true")))
