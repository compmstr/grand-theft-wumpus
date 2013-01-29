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

(player/start-player game-map)
(print (game-dot-string))
(ui/start-ui)
(gviz/render-dot-string (game-dot-string) "/tmp/gtw.game-map.png")
;;Delete the map file when we're done with the game
(.deleteOnExit (java.io.File. "/tmp/gtw.game-map.png"))

(ui/set-img "/tmp/gtw.game-map.png")

;;(player/go-to game-map <new-loc>)

;;(gviz/render-dot-string (game-dot-string) "game-map.png")
;; then update the image on the UI

;;Example of displaying map
;;(spit "map.dot"
      ;;(-> (city-map/generate-map)
          ;;(city-map/map->graph)
          ;;(gviz/graph->dot :concentrate "true")))