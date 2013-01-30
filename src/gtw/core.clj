(ns gtw.core
  (:require [gtw.city-map :as city-map]
            [gtw.player :as player]
            [gtw.ui :as ui]
            gviz))

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
  (gviz/render-dot-string (game-dot-string) "/tmp/gtw.game-map.png")
  (ui/set-img "/tmp/gtw.game-map.png")
  (ui/set-buttons (apply hash-map
                         (apply concat
                                (for [conn
                                      (:connections
                                       (city-map/id->loc game-map new-loc))]
                                  [conn #(game-move conn)])))))
(defn game-move
  [new-loc]
  (player/go-to game-map new-loc)
  (game-display (:loc @player/player)))

(player/start-player game-map)
(ui/start-ui)
(game-display (:loc @player/player))
(.deleteOnExit (java.io.File. "/tmp/gtw.game-map.png"))
;;(print (game-dot-string))
;;(ui/start-ui)
;;(gviz/render-dot-string (game-dot-string) "/tmp/gtw.game-map.png")
;;Delete the map file when we're done with the game

;;(ui/set-img "/tmp/gtw.game-map.png")

;;(player/go-to game-map <new-loc>)

;;(gviz/render-dot-string (game-dot-string) "game-map.png")
;; then update the image on the UI

;;Example of displaying map
;;(spit "map.dot"
      ;;(-> (city-map/generate-map)
          ;;(city-map/map->graph)
          ;;(gviz/graph->dot :concentrate "true")))